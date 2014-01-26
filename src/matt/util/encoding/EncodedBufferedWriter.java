package matt.util.encoding;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class EncodedBufferedWriter implements Closeable, Flushable
{
	private MAWCharacterEncoding encoding;
	private BufferedWriter bw;
	private String lineSeparator;
	
	public EncodedBufferedWriter(Writer writer, MAWCharacterEncoding encoding, int bufferSize)
	{
		bw = new BufferedWriter(writer, bufferSize);
		this.encoding = encoding;
		lineSeparator = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
		lineSeparator = encoding.encode(lineSeparator);
	}
	
	public EncodedBufferedWriter(Writer writer, MAWCharacterEncoding encoding)
	{
		this(writer, encoding, 8192);
	}
	
	public void write(int c) throws IOException
	{
		char[] ca = encoding.encode("" + (char) c).toCharArray();
		for(char charr : ca)
		{
			bw.write(charr);
		}
	}
	
	public void write(String s) throws IOException
	{
		s = encoding.encode(s);
		bw.write(s);
	}
	
	public void newLine() throws IOException
	{
		bw.write(lineSeparator);
	}
	
	@Override
	public void flush() throws IOException
	{
		bw.flush();
	}
	
	@Override
	public void close() throws IOException
	{
		bw.close();
	}
}