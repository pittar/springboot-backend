package ca.pitt.demo.spring.rest.backend.value;

/**
 * RandomValue.
 * 
 * @author Andrew Pitt
 * @since 1.0.0
 */
public class RandomValue {

	/** Value. */
	private Integer value;
	
	/**
	 * Public constructor.
	 * 
	 * @param aValue <code>Integer</code>
	 */
	public RandomValue(Integer aValue) {
		this.value = aValue;
	}

	/**
	 * Get value.
	 * 
	 * @return <code>Integer</code>
	 */
	public Integer getValue() {
		return value;
	}
	
	
	
}
