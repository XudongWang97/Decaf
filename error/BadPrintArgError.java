package decaf.error;

import decaf.Location;

/**
 * example：incompatible argument 3: int[] given, int/bool/string expected<br>
 * 3表示发生错误的是第三个参数<br>
 * PA2
 */
public class BadPrintArgError extends DecafError {

	private String count;

	private String type;
	private boolean ptcomp;

	public BadPrintArgError(Location location, String count, String type) {
		super(location);
		this.count = count;
		this.type = type;
		this.ptcomp=false;
	}
	
	public BadPrintArgError(Location location, String count, String type,boolean ptcomp) {
		super(location);
		this.count = count;
		this.type = type;
		this.ptcomp=true;
	}

	@Override
	protected String getErrMsg() {
		if(!this.ptcomp)
		{
			return "incompatible argument " + count + ": " + type + " given, int/bool/string expected";
		}else
		{
			return "incompatible argument " + count + ": " + type + " given, complex expected";
		}
	}

}
