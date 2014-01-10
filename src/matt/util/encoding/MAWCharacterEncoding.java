package matt.util.encoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class MAWCharacterEncoding
{
	public static final String VERSION = "v0.0.1";
	private static final List<TreeMap<Short, Character>> encodings = new ArrayList<TreeMap<Short, Character>>();
	public static final MAWCharacterEncoding INSTANCE = new MAWCharacterEncoding();
	
	private MAWCharacterEncoding()
	{
		// Registers english as encoding 1
		addMapping("abcdefghijklmnopqrstuvwxyz+-*/= ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&()_`~[{]};:\'\",<.>?\\|\n\t\b");
		// Registers japanese as encoding 2
		addMapping("ーぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ゛゜");
	}
	
	public void addMapping(String s)
	{
		if(s.length() > 255)
			throw new IllegalArgumentException("Cannot have more than 255 mappings per map.");
		
		TreeMap<Short, Character> map = new TreeMap<Short, Character>();
		for(short i = 0; i < 255; i++)
		{
			if(i < s.length())
				map.put(i, s.charAt(i));
			else
				map.put(i, null);
		}
		encodings.add(map);
	}
	
	public void appendMapping(int mappingId, String s)
	{
		if(mappingId >= encodings.size())
			throw new NullPointerException("Mapping with id " + mappingId + " does not exist. Please use addMapping(java.lang.String) to add a new mapping.");
		if(s == null)
			throw new IllegalArgumentException("Mappings cannot be null");
		if(getMappingSize(mappingId) + s.length() > 255)
			throw new IllegalArgumentException("Mapping with id " + mappingId + " only has " + (255 - getMappingSize(mappingId)) + " free mappings. You tried to put " + s.length() + " mappings into it.\nPlease use addMapping(java.lang.String) to add a new mapping.");
		
		short size = (short) getMappingSize(mappingId);
		for(short i = size; i < 255 && (i - size) < s.length(); i++)
		{
			encodings.get(mappingId).put(i, s.charAt(i - size));
		}
	}
	
	public short[] encode(String data)
	{
		List<Short> encodedData = new ArrayList<Short>();
		for(int i = 0; i < data.length(); i++)
		{
			char c = data.charAt(i);
			int encodingMapId = 0;
			while(encodingMapId < encodings.size() && !encodings.get(encodingMapId).containsValue(c))
				encodingMapId++;
			if(encodingMapId >= encodings.size())
			{
				System.err.println("ERROR: There is no mapping for character \'" + c + "\'");
				System.err.println("Please contact the author of this encoding and give him the following information.");
				System.err.println("Version: " + VERSION + "\nCharacter: " + c);
				return encode(data.substring(0, i) + data.substring(i + 1));
			}
			for(int j = 0; j < encodingMapId; j++)
			{
				encodedData.add((short) 255);
			}
			Set<Entry<Short, Character>> s = encodings.get(encodingMapId).entrySet();
			for(Entry<Short, Character> e : s)
			{
				if(e.getValue().equals(c))
				{
					encodedData.add(e.getKey());
					break;
				}
			}
		}
		short[] res = new short[encodedData.size()];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = encodedData.get(i).shortValue();
		}
		return res;
	}
	
	public String decode(short[] data)
	{
		String res = "";
		for(int i = 0; i < data.length; i++)
		{
			int encodingMapId = 0;
			while(data[i] == 255)
			{
				encodingMapId++;
				i++;
			}
			res += encodings.get(encodingMapId).get(data[i]).charValue();
		}
		return res;
	}
	
	public short[] asRawData(String data)
	{
		char[] c = data.toCharArray();
		short[] res = new short[c.length];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = (short) c[i];
		}
		return res;
	}
	
	public String asSaveableData(short[] data)
	{
		if(data == null)
			throw new IllegalArgumentException("Data cannot be null");
		String res = "";
		for(int i = 0; i < data.length; i++)
		{
			res += (char) data[i];
		}
		return res;
	}
	
	public int getMappingSize(int mappingId)
	{
		if(mappingId >= encodings.size())
			throw new NullPointerException("Mapping with id " + mappingId + " does not exist.");
		int offset = 0;
		for(Character c : encodings.get(mappingId).values())
		{
			if(c == null)
				offset++;
		}
		return 255 - offset;
	}
}