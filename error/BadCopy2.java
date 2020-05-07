package decaf.error;

import decaf.Location;

/**
 * example：test expression must have bool type<br>
 * PA2
 */
public class BadCopy2 extends DecafError {
	
	String type, deType;

	public BadCopy2(Location location, String type, String deType) {
		super(location);
		this.type=type;
		this.deType=deType;
	}

	@Override
	protected String getErrMsg() {
		return "For copy expr, the source "+type+" and the destination "+deType+" are not same";
	}

}
