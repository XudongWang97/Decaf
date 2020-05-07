package decaf.error;

import decaf.Location;

/**
 * example：test expression must have bool type<br>
 * PA2
 */
public class NotUniqueCondition extends DecafError {

	public NotUniqueCondition(Location location) {
		super(location);
	}

	@Override
	protected String getErrMsg() {
		return "condition is not unique";
	}

}
