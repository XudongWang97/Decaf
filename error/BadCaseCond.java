package decaf.error;

import decaf.Location;

public class BadCaseCond extends DecafError {
	
	String type;

	public BadCaseCond(Location location, String type) {
		super(location);
		this.type = type;
	}

	@Override
	protected String getErrMsg() {
		return "incompatible case expr: " + type + " given, but int expected";
	}

}
