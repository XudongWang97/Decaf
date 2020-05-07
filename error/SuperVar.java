package decaf.error;

import decaf.Location;

/**
 * can not use super in static function
 * PA2
 */
public class SuperVar extends DecafError {

	public SuperVar(Location location) {
		super(location);
	}

	@Override
	protected String getErrMsg() {
		return "super.member_var is not supported";
	}

}
