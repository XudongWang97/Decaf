package decaf.error;

import decaf.Location;

/**
 * example：test expression must have bool type<br>
 * PA2
 */
public class BadCopy extends DecafError {
	
	String type;

	public BadCopy(Location location, String type) {
		super(location);
		this.type = type;
	}
	@Override
	protected String getErrMsg() {
		return "expected class type for copy expr but " + type + " given";
	}

}
