package tech.lapsa.insurance.calculation.beans;

import static tech.lapsa.java.commons.function.MyMaps.*;

import java.util.Map;

import com.lapsa.insurance.elements.InsuranceClassType;
import com.lapsa.insurance.elements.InsuredAgeClass;
import com.lapsa.insurance.elements.InsuredExpirienceClass;
import com.lapsa.insurance.elements.PolicyTemporaryEntryTimeCategory;
import com.lapsa.insurance.elements.VehicleAgeClass;
import com.lapsa.insurance.elements.VehicleClass;
import com.lapsa.kz.country.KZArea;

public final class PolicyRates {

    public static final double MRP = 2405d;
    public static final double BASE_RATE = 1.9;

    public static final Map<PolicyTemporaryEntryTimeCategory, Double> POLICY_TEMPORARY_ENTRIY_TIME_CATEGORY_RATES = ofEntries(
	    entry(PolicyTemporaryEntryTimeCategory.TO_15D_INCL, 0.20d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_16D_TO_1M_INCL, 0.30d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_1M_TO_2M_INCL, 0.40d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_2M_TO_3M_INCL, 0.50d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_3M_TO_4M_INCL, 0.60d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_4M_TO_5M_INCL, 0.65d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_5M_TO_6M_INCL, 0.70d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_6M_TO_7M_INCL, 0.80d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_7M_TO_8M_INCL, 0.90d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_8M_TO_9M_INCL, 0.95d),
	    entry(PolicyTemporaryEntryTimeCategory.FROM_9M, 1.00d));

    public static final Map<KZArea, Double> REGION_RATES = ofEntries(
	    entry(KZArea.OALM, 1.78d), // 05
	    entry(KZArea.OTRK, 1.01d), // 13
	    entry(KZArea.OVK, 1.96d), // 16
	    entry(KZArea.OKST, 1.95d), // 10
	    entry(KZArea.OKGD, 1.39d), // 09
	    entry(KZArea.OSK, 1.33d), // 15
	    entry(KZArea.OAKM, 1.32d), // 03
	    entry(KZArea.OPVL, 1.63d), // 14
	    entry(KZArea.OZHM, 1.00d), // 08
	    entry(KZArea.OAKT, 1.35d), // 04
	    entry(KZArea.OZK, 1.17d), // 07
	    entry(KZArea.OKZL, 1.09d), // 11
	    entry(KZArea.OATY, 2.69d), // 06
	    entry(KZArea.OMNG, 1.15d), // 12
	    entry(KZArea.GALM, 2.96d), // 02
	    entry(KZArea.GAST, 2.20d), // 01
	    entry(KZArea.GSHM, 1.01d) // 17
    );

    public static final double NON_MAJOR_CITY_CORRECTION_RATE = 0.8d;

    public static final Map<VehicleClass, Double> VEHICLE_CLASS_RATES = ofEntries(
	    entry(VehicleClass.CAR, 2.09d),
	    entry(VehicleClass.BUS16, 3.26d),
	    entry(VehicleClass.BUSOVR16, 3.45d),
	    entry(VehicleClass.CARGO, 3.98d),
	    entry(VehicleClass.TRAM, 2.33d),
	    entry(VehicleClass.MOTO, 1.00d),
	    entry(VehicleClass.TRAILER, 1.00d));

    public static final Map<InsuredAgeClass, Map<InsuredExpirienceClass, Double>> INSURED_AGE_EXPIRIENCE_CLASS_RATES = ofEntries(
	    entry(InsuredAgeClass.UNDER25, ofEntries(
		    entry(InsuredExpirienceClass.LESS2, 1.1d),
		    entry(InsuredExpirienceClass.MORE2, 1.05d))),
	    entry(InsuredAgeClass.OVER25, ofEntries(
		    entry(InsuredExpirienceClass.LESS2, 1.05d),
		    entry(InsuredExpirienceClass.MORE2, 1.0d))));

    public static final Map<VehicleAgeClass, Double> VEHICLE_AGE_CLASS_RATES = ofEntries(
	    entry(VehicleAgeClass.UNDER7, 1.0d),
	    entry(VehicleAgeClass.OVER7, 1.1d));

    public static final Map<InsuranceClassType, Double> INSURANCE_CLASS_TYPE_RATES = ofEntries(
	    entry(InsuranceClassType.CLASS_M, 2.45d),
	    entry(InsuranceClassType.CLASS_0, 2.30d),
	    entry(InsuranceClassType.CLASS_1, 1.55d),
	    entry(InsuranceClassType.CLASS_2, 1.40d),
	    entry(InsuranceClassType.CLASS_3, 1.00d),
	    entry(InsuranceClassType.CLASS_4, 0.95d),
	    entry(InsuranceClassType.CLASS_5, 0.90d),
	    entry(InsuranceClassType.CLASS_6, 0.85d),
	    entry(InsuranceClassType.CLASS_7, 0.80d),
	    entry(InsuranceClassType.CLASS_8, 0.75d),
	    entry(InsuranceClassType.CLASS_9, 0.70d),
	    entry(InsuranceClassType.CLASS_10, 0.65d),
	    entry(InsuranceClassType.CLASS_11, 0.60d),
	    entry(InsuranceClassType.CLASS_12, 0.55d),
	    entry(InsuranceClassType.CLASS_13, 0.50d));

    public static final double PRIVILEGER_RATE = 0.5d;

    public static final double TEMPORARY_ENTRY_RATE = 4.4d;
}
