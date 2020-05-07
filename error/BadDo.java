package decaf.error;

import decaf.Location;

public class BadDo extends DecafError {

	String type;
	public BadDo(Location location, String type) {
		super(location);
		this.type=type;
	}
	@Override
	protected String getErrMsg() {
		return "The condition of Do Stmt requestd type bool but "+type+" given";
	}

}
