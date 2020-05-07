package decaf.error;

import decaf.Location;
public class BadCaseExpr extends DecafError {
	
	String type,defType ;

	public BadCaseExpr(Location location, String type, String defType) {
		super(location);
		this.type=type;
		this.defType=defType;
	}

	@Override
	protected String getErrMsg() {
		return "type: " + type + " is different with other expr's type " + defType;
	}

}
