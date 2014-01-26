package matt.util.encoding.exceptions;

public class MapCollisionException extends NullPointerException
{
	private static final long serialVersionUID = -1425250473759343609L;
	
	public MapCollisionException(String message)
	{
		super(message);
	}
	
	public MapCollisionException(int offset, int mappingId, int index)
	{
		this("The offset of " + offset + " caused a collision in map " + mappingId + " at index " + index);
	}
	
	public MapCollisionException()
	{
		super();
	}
}