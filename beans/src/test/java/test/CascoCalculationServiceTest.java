package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static test.HelperUtilityClass.*;

import java.util.Currency;

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
	assertThat(casco.getCalculation().getAmount(), equalTo(282994d));
	assertThat(casco.getCalculation().getCurrency(), allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

    @Test
    public void testCascoCalculationVariant2() throws CalculationFailed {
	Casco casco = generateCasco();
	casco.setDeductiblePartialRequired(true);
	casco.setDeductiblePartialRate(CascoDeductiblePartialRate.PERCENT2);
	calc.calculateCascoCost(casco);
	assertThat(casco.getCalculation().getAmount(), equalTo(177375d));
	assertThat(casco.getCalculation().getCurrency(), allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
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
	assertThat(casco.getCalculation().getAmount(), equalTo(50000.00d));
	assertThat(casco.getCalculation().getCurrency(), allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

}
