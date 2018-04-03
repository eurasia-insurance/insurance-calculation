package tech.lapsa.insurance.calculation.beans;

import static tech.lapsa.insurance.calculation.beans.Calculations.*;
import static tech.lapsa.insurance.calculation.beans.PolicyRates.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.OptionalDouble;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.lapsa.insurance.domain.CalculationData;
import com.lapsa.insurance.domain.InsurancePeriodData;
import com.lapsa.insurance.domain.policy.Policy;
import com.lapsa.insurance.domain.policy.PolicyDriver;
import com.lapsa.insurance.domain.policy.PolicyVehicle;
import com.lapsa.insurance.elements.InsuranceClassType;
import com.lapsa.insurance.elements.InsuredAgeClass;
import com.lapsa.insurance.elements.InsuredExpirienceClass;
import com.lapsa.insurance.elements.PolicyTemporaryEntryTimeCategory;
import com.lapsa.insurance.elements.VehicleAgeClass;
import com.lapsa.insurance.elements.VehicleClass;
import com.lapsa.kz.country.KZArea;

import tech.lapsa.insurance.calculation.CalculationFailed;
import tech.lapsa.insurance.calculation.PolicyCalculation;
import tech.lapsa.insurance.calculation.PolicyCalculation.PolicyCalculationLocal;
import tech.lapsa.insurance.calculation.PolicyCalculation.PolicyCalculationRemote;
import tech.lapsa.java.commons.function.MyExceptions;
import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.java.commons.function.MyOptionals;

@Stateless(name = PolicyCalculation.BEAN_NAME)
public class PolicyCalculationBean implements PolicyCalculationLocal, PolicyCalculationRemote {

    @Resource(lookup = Constants.JNDI_RESOURCE_CONFIGURATION)
    private Properties configurationProperties;

    private NavigableMap<LocalDate, Double> mrps;

    @PostConstruct
    public void loadMrps() {
	mrps = new TreeMap<>();
	for (final Entry<Object, Object> e : configurationProperties.entrySet()) {
	    final LocalDate date = LocalDate.parse(e.getKey().toString());
	    final Double value = Double.valueOf(e.getValue().toString());
	    mrps.put(date, value);
	}
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void calculatePolicyCost(final Policy policy) throws CalculationFailed {
	_calculate(policy, policy.getCalculation());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CalculationData calculateAmount(final Policy policy) throws CalculationFailed {
	final CalculationData res = new CalculationData();
	_calculate(policy, res);
	return res;
    }

    // PRIVATE

    private void _calculate(final Policy policy,
	    final CalculationData calc) throws CalculationFailed {
	double cost = policyCost(policy.getInsuredDrivers(),
		policy.getInsuredVehicles(), policy.getPeriod());
	calc.setAmount(cost);
	calc.setCurrency(Currency.getInstance("KZT"));
    }

    private static enum InsuranceType {
	STANDARD, COMPLEX;
    }

    private double policyCost(final List<PolicyDriver> drivers,
	    final List<PolicyVehicle> vehicles,
	    final InsurancePeriodData period) throws CalculationFailed {
	final InsuranceType insuranceType = (vehicles.size() == 1 && drivers.size() >= 1)
		? InsuranceType.STANDARD
		: (vehicles.size() >= 1 && drivers.size() == 1)
			? InsuranceType.COMPLEX
			: null;
	if (insuranceType == null)
	    throw MyExceptions.format(CalculationFailed::new, "Can't determine the type of insurance (%1$s)",
		    Arrays.asList(InsuranceType.values()));
	double maximumCost = 0d;
	for (final PolicyDriver driver : drivers)
	    for (final PolicyVehicle vehicle : vehicles) {
		final double cost = policyCostVariant(driver, vehicle, period, insuranceType, drivers.size());
		if (maximumCost < cost)
		    maximumCost = cost;
	    }
	return maximumCost;
    }

    private OptionalDouble getMRPOn(final LocalDate date) {
	MyObjects.requireNonNull(date, "date");
	return MyOptionals.of(mrps) //
		.map(map -> map.floorEntry(date))
		.map(Entry::getValue) //
		.map(OptionalDouble::of) //
		.orElseGet(OptionalDouble::empty);
    }

    private double policyCostVariant(final PolicyDriver insured,
	    final PolicyVehicle vehicle,
	    final InsurancePeriodData period,
	    final InsuranceType insuranceType,
	    final int numberOfInsuredPersons) throws CalculationFailed {

	double cost = 0d;

	{
	    // МРП
	    final LocalDate date = MyOptionals.of(period) //
		    .map(InsurancePeriodData::getFrom) //
		    .orElseGet(LocalDate::now);
	    cost = getMRPOn(date)
		    .orElseThrow(
			    MyExceptions.supplier(EJBException::new, "MRP settings is not available on %1$s", date));
	}

	{
	    // базовая страховая премия
	    double rate = getBaseRate();
	    cost *= rate;
	}

	// Если временный въезд, то не учитывается:
	// - регион
	// - город (мажорность города)
	// - класс страхования (бонус-малус)
	// Gомимо этого, при временном въезде используется другая форула расчета
	// стоимости страхования на короткие сроки (до года)

	if (vehicle.isTemporaryEntry()) {

	    // для авто ВРЕМЕННО въезжающих на территорию РК (т.е. не состоящих
	    // на учете в РК)

	    {
		// коэффициент по временному въезду
		double rate = getTemporaryEntryRate();
		cost *= rate;
	    }

	    {
		// коэффициент по сроку страхования
		if (period != null) {
		    PolicyTemporaryEntryTimeCategory timeClass = detectTemporaryEntryTimeCategory(period);
		    double rate = getPolicyTemporaryEntryTimeCategoryRate(timeClass);
		    cost *= rate;
		}
	    }

	} else {

	    // для авто состоящих на учете РК

	    {
		// коэффициент по Региону
		double rate = getRegionRate(vehicle.getArea());
		cost *= rate;
	    }

	    {
		// коэффициент по городу регионального значения
		final Boolean isMajorCity;
		if (vehicle.getCity() != null) {
		    isMajorCity = isMajorCity(vehicle.getCity());
		} else {
		    if ((vehicle.getArea() == KZArea.GALM || vehicle.getArea() == KZArea.GAST)
			    && !vehicle.isForcedMajorCity())
			throw new CalculationFailed(String.format(
				"City for area %1$s must be major city or city must be set", vehicle.getArea().name()));
		    isMajorCity = vehicle.isForcedMajorCity();
		}

		if (!isMajorCity) {
		    double rate = getNonMajorCityCorrectionRate();
		    cost *= rate;
		}
	    }

	    {
		// коэффициент по классу страхователя (бонус-малус)
		double rate = getInsuranceClassTypeRate(insured.getInsuranceClassType());
		cost *= rate;
	    }
	}

	{
	    // коэффициент по типу ТС
	    double rate = getVehicleTypeRate(vehicle.getVehicleClass());
	    cost *= rate;
	}

	{
	    // коэффициент по "опыту" водителя (возраст/водительский стаж)
	    double rate = getDriverExpirienceTypeRate(insured.getAgeClass(), insured.getExpirienceClass());
	    cost *= rate;
	}

	{
	    // коэффициент по "возрасту" авто
	    double rate = getVehicleAgeTypeRate(vehicle.getVehicleAgeClass());
	    cost *= rate;
	}

	{
	    // скидка привелегированным (пенсионеры, участники, инвалиды)
	    // предоставляется только по стандартному договору страхования (не
	    // комплексному) и только в случае, если застрахованный водитель
	    // один
	    if (insuranceType == InsuranceType.STANDARD
		    && numberOfInsuredPersons == 1
		    && insured.isHasAnyPrivilege()) {
		double rate = getPrivilegerRate();
		cost *= rate;
	    }
	}

	{
	    // неполный период страхования
	    if (period != null && !vehicle.isTemporaryEntry())
		cost = costAnnualToPeriod(cost, period);
	}

	// округляем до "копеек"
	cost = roundMoney(cost);

	return cost;
    }

    private static double getBaseRate() {
	return BASE_RATE;
    }

    private static double getPolicyTemporaryEntryTimeCategoryRate(
	    final PolicyTemporaryEntryTimeCategory contractTimeClass)
	    throws CalculationFailed {
	assertNotNull(contractTimeClass, PolicyTemporaryEntryTimeCategory.class);
	assertHasKey(contractTimeClass, POLICY_TEMPORARY_ENTRIY_TIME_CATEGORY_RATES,
		PolicyTemporaryEntryTimeCategory.class);
	return POLICY_TEMPORARY_ENTRIY_TIME_CATEGORY_RATES.get(contractTimeClass);
    }

    private static double getRegionRate(final KZArea region) throws CalculationFailed {
	assertNotNull(region, KZArea.class);
	assertHasKey(region, REGION_RATES, KZArea.class);
	return REGION_RATES.get(region);
    }

    private static double getVehicleTypeRate(final VehicleClass vehicleType) throws CalculationFailed {
	assertNotNull(vehicleType, VehicleClass.class);
	assertHasKey(vehicleType, VEHICLE_CLASS_RATES, VehicleClass.class);
	return VEHICLE_CLASS_RATES.get(vehicleType);
    }

    private static double getDriverExpirienceTypeRate(final InsuredAgeClass ageClass,
	    final InsuredExpirienceClass driverExpirienceClass) throws CalculationFailed {
	assertNotNull(ageClass, InsuredAgeClass.class);
	assertNotNull(driverExpirienceClass, InsuredExpirienceClass.class);
	assertHasKey(ageClass, INSURED_AGE_EXPIRIENCE_CLASS_RATES, InsuredAgeClass.class);
	assertHasKey(driverExpirienceClass, INSURED_AGE_EXPIRIENCE_CLASS_RATES.get(ageClass),
		InsuredExpirienceClass.class);
	return INSURED_AGE_EXPIRIENCE_CLASS_RATES.get(ageClass).get(driverExpirienceClass);
    }

    private static double getVehicleAgeTypeRate(final VehicleAgeClass vehicleAgeType) throws CalculationFailed {
	assertNotNull(vehicleAgeType, VehicleAgeClass.class);
	assertHasKey(vehicleAgeType, VEHICLE_AGE_CLASS_RATES, VehicleAgeClass.class);
	return VEHICLE_AGE_CLASS_RATES.get(vehicleAgeType);
    }

    private static double getInsuranceClassTypeRate(final InsuranceClassType insuranceClassType)
	    throws CalculationFailed {
	assertNotNull(insuranceClassType, InsuranceClassType.class);
	assertHasKey(insuranceClassType, INSURANCE_CLASS_TYPE_RATES, InsuranceClassType.class);
	return INSURANCE_CLASS_TYPE_RATES.get(insuranceClassType);
    }

    private static double getTemporaryEntryRate() {
	return TEMPORARY_ENTRY_RATE;
    }

    private static double getPrivilegerRate() throws CalculationFailed {
	return PRIVILEGER_RATE;
    }

    private static double getNonMajorCityCorrectionRate() throws CalculationFailed {
	return NON_MAJOR_CITY_CORRECTION_RATE;
    }
}
