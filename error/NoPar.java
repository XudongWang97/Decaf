package decaf.error;

import decaf.Location;

/**
 * can not use super in static function
 * PA2
 */
public class NoPar extends DecafError {
	
	String type;

	public NoPar(Location location, String classType) {
		super(location);
		this.type=classType;
	}

	@Override
	protected String getErrMsg() {
		return "no parent class exist for "+type;
	}

}
