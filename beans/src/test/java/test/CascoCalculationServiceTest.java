package test;

import static test.HelperUtilityClass.*;

import javax.inject.Inject;

import org.junit.Test;

import com.lapsa.insurance.domain.casco.Casco;
import com.lapsa.insurance.elements.CascoCarAgeClass;
import com.lapsa.insurance.elements.CascoDeductibleFullRate;
import com.lapsa.insurance.elements.CascoDeductiblePartialRate;

import tech.lapsa.insurance.calculation.CalculationFailed;
import tech.lapsa.insurance.calculation.CascoCalculation.CascoCalculationLocal;

public class CascoCalculationServiceTest extends ArquillianBaseTestCase {

    @Inject
    private CascoCalculationLocal calc;

    @Test
    public void testCascoCalculationVariant1() throws CalculationFailed {
	Casco casco = generateCasco();

	calc.calculateCascoCost(casco);
	assertExpectingAmount(casco, 282994d);
    }

    @Test
    public void testCascoCalculationVariant2() throws CalculationFailed {
	Casco casco = generateCasco();
	casco.setDeductiblePartialRequired(true);
	casco.setDeductiblePartialRate(CascoDeductiblePartialRate.PERCENT2);

	calc.calculateCascoCost(casco);
	assertExpectingAmount(casco, 177375d);
    }

    @Test
    public void testCascoCalculationVariant3() throws CalculationFailed {
	Casco casco = generateCasco();
	casco.setDeductiblePartialRequired(true);
	casco.setDeductiblePartialRate(CascoDeductiblePartialRate.PERCENT10);
	casco.getInsuredVehicle().setCarAgeClass(CascoCarAgeClass.UNDER3);
	casco.setNoPoliceCallRequired(false);
	casco.setNoGuiltNoDeductibleRequired(false);
	casco.setDeductibleFullRate(CascoDeductibleFullRate.PERCENT10);
	casco.setReplacementCarRequired(false);

	casco.setCoverRoadAccidents(false);
	casco.setCoverNonRoadAccidents(true);

	calc.calculateCascoCost(casco);
	assertExpectingAmount(casco, 50000d);
    }
}
