package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Currency;

import com.lapsa.insurance.domain.CalculationData;
import com.lapsa.insurance.domain.InsuranceProduct;
import com.lapsa.insurance.domain.casco.Casco;
import com.lapsa.insurance.domain.casco.CascoVehicle;
import com.lapsa.insurance.domain.policy.Policy;
import com.lapsa.insurance.domain.policy.PolicyDriver;
import com.lapsa.insurance.domain.policy.PolicyVehicle;
import com.lapsa.insurance.elements.CascoCarAgeClass;
import com.lapsa.insurance.elements.CascoDeductibleFullRate;
import com.lapsa.insurance.elements.InsuranceClassType;
import com.lapsa.insurance.elements.InsuredAgeClass;
import com.lapsa.insurance.elements.InsuredExpirienceClass;
import com.lapsa.insurance.elements.VehicleAgeClass;
import com.lapsa.insurance.elements.VehicleClass;
import com.lapsa.kz.country.KZArea;
import com.lapsa.kz.country.KZCity;

import tech.lapsa.java.commons.function.MyStrings;
import tech.lapsa.kz.taxpayer.TaxpayerNumber;

public final class HelperUtilityClass {

    public static Policy generatePolicy() {
	Policy policy = new Policy();

	PolicyDriver d = generatePolicyDriver();
	policy.addDriver(d);

	PolicyVehicle v = generatePolicyVehicle();
	policy.addVehicle(v);

	CalculationData c = new CalculationData();
	policy.setCalculation(c);

	return policy;
    }

    public static PolicyVehicle generatePolicyVehicle() {
	PolicyVehicle v = new PolicyVehicle();
	v.setCity(KZCity.ALM);
	v.setArea(KZArea.GALM);
	v.setVehicleClass(VehicleClass.CAR);
	v.setVehicleAgeClass(VehicleAgeClass.OVER7);
	return v;
    }

    public static PolicyDriver generatePolicyDriver() {
	PolicyDriver d = new PolicyDriver();
	d.setIdNumber(TaxpayerNumber.of("123123123127"));
	d.setInsuranceClassType(InsuranceClassType.CLASS_3);
	d.setAgeClass(InsuredAgeClass.OVER25);
	d.setExpirienceClass(InsuredExpirienceClass.LESS2);
	d.setHasAnyPrivilege(false);
	return d;
    }

    public static Casco generateCasco() {
	Casco casco = new Casco();
	casco.setCalculation(new CalculationData());

	casco.setInsuredVehicle(new CascoVehicle());
	casco.getInsuredVehicle().setCost(5000000d);
	casco.getInsuredVehicle().setYearOfManufacture(2008);
	casco.getInsuredVehicle().setCarAgeClass(CascoCarAgeClass.FROM3TO7);
	casco.getInsuredVehicle().setArea(KZArea.GALM);
	casco.getInsuredVehicle().setCity(KZCity.ALM);

	casco.setDeductiblePartialRequired(false);

	casco.setSpecialServiceStationRequired(true);
	casco.setNoPoliceCallRequired(true);
	casco.setNoGuiltNoDeductibleRequired(true);
	casco.setDeductibleFullRate(CascoDeductibleFullRate.PERCENT5);
	casco.setHelpWithPoliceRequired(true);
	casco.setEvacuatorRequired(true);
	casco.setReplacementCarRequired(true);

	casco.setCoverRoadAccidents(true);
	casco.setCoverNonRoadAccidents(false);
	casco.setContractEndsAfterFirstCase(false);

	casco.setThirdPartyLiabilityCoverage(false);
	casco.setDriverAndPassengerCoverage(false);
	casco.setDriverAndPassengerCount(0);
	return casco;
    }

    public static void assertExpectingAmount(final InsuranceProduct product, final double expectedAmount) {
	assertThat(product.getCalculation().getAmount(), equalTo(expectedAmount));
	assertThat(product.getCalculation().getCurrency(),
		allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
	System.out.println(MyStrings.format("Expected amount of %1$.2f", expectedAmount));
    }
}
