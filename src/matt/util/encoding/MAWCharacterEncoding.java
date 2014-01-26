package matt.util.encoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import matt.util.encoding.exceptions.MapCollisionException;

public class MAWCharacterEncoding
{
	public static final String VERSION = "v0.2.0";
	protected final List<TreeMap<Integer, Character>> encodings = new ArrayList<TreeMap<Integer, Character>>();
	
	public MAWCharacterEncoding(File mappingFile)
	{
		if(!registeredFromFile(mappingFile))
		{
			// Registers english as encoding 1
			addMapping("abcdefghijklmnopqrstuvwxyz+-*/= ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&()_`~[{]};:\'\",<.>?\\|\n\t\b\r");
			// Registers japanese as encoding 2
			appendMapping(0, "ーぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ゛゜");
		}
	}
	
	public MAWCharacterEncoding()
	{
		this(null);
	}
	
	/**
	 * Not sure why I left this in here. Well, this was the default file originally.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private boolean registeredFromFile()
	{
		return registeredFromFile(new File("MAWMapping.mawenc"));
	}
	
	private boolean registeredFromFile(File file)
	{
		try
		{
			if(!file.exists())
				file.createNewFile();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = "";
			String s;
			while((s = br.readLine()) != null)
				str += "\n" + s;
			br.close();
			if(str.replace("\n", "").isEmpty())
				return false;
			String[] sa = str.split("\n");
			for(String string : sa)
			{
				if(string != null && !string.isEmpty())
					addMapping(fixFileMapping(string));
			}
		}
		catch(NullPointerException e)
		{
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String fixFileMapping(String s)
	{
		return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\f", "\f").replace("\\b", "\b").replace("\\t", "\t").replace("\\\'", "\'").replace("\\\"", "\"").replace("\\\\", "\\");
	}
	
	public void addMapping(String s, int offset)
	{
		if(s.length() > 65535)
			throw new IllegalArgumentException("Cannot have more than 65535 mappings per map.");
		if(offset < 0)
			throw new IllegalArgumentException("Offset cannot be less than 0");
		if(s.length() + offset > 65535)
			throw new IllegalArgumentException("Offset cannot push the mapping above 65535");
		
		TreeMap<Integer, Character> map = new TreeMap<Integer, Character>();
		for(int i = 0; i < 65535; i++)
		{
			if(i < s.length() + offset && i >= offset)
				map.put(i, s.charAt(i - offset));
			else
				map.put(i, null);
		}
		encodings.add(map);
	}
	
	public void addMapping(String s)
	{
		addMapping(s, 0);
	}
	
	/**
	 * appends the given string to the mapping with the given id
	 * @param mappingId
	 * @param map
	 * @param offset if the number is less than 0 or greater than 65535, it will try to fit it in any available space. otherwise it will append the mapping at the given offset.
	 */
	public void appendMapping(int mappingId, String map, int offset)
	{
		if(offset < 0 || offset > 65535)
			offset = -1;
		if(mappingId >= encodings.size())
			throw new NullPointerException("Mapping with id " + mappingId + " does not exist. Please use addMapping(java.lang.String) to add a new mapping.");
		if(map == null)
			throw new IllegalArgumentException("Mappings cannot be null");
		if(offset == -1)
		{
			if(getFreeMappingSpaces(mappingId) + map.length() > 65535)
				throw new IllegalArgumentException("Mapping with id " + mappingId + " only has " + (65535 - getFreeMappingSpaces(mappingId)) + " free mappings. You tried to put " + map.length() + " mappings into it.\nPlease use addMapping(java.lang.String) to add a new mapping.");
			int i = 0;
			int freeMapLoc;
			while(i < map.length() && (freeMapLoc = getNextFreeMappingSpace(mappingId)) != -1)
			{
				encodings.get(mappingId).put(freeMapLoc, map.charAt(i++));
			}
		}
		else
		{
			if(map.length() + offset > 65535)
				throw new IndexOutOfBoundsException("Offset cannot cause map length to exceed 65535");
			for(int i = offset; i < map.length() + offset; i++)
			{
				if(encodings.get(mappingId).get(i) != null)
					throw new MapCollisionException(offset, mappingId, i);
			}
			for(int i = offset; i < map.length() + offset; i++)
			{
				encodings.get(mappingId).put(i, map.charAt(i - offset));
			}
		}
	}
	
	public void appendMapping(int mappingId, String map)
	{
		appendMapping(mappingId, map, getMappingSize(mappingId));
	}
	
	public String encode(String data)
	{
		List<Integer> encodedData = new ArrayList<Integer>();
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
				continue;
			}
			for(int j = 0; j < encodingMapId; j++)
			{
				encodedData.add(65535);
			}
			Set<Entry<Integer, Character>> s = encodings.get(encodingMapId).entrySet();
			for(Entry<Integer, Character> e : s)
			{
				if(e.getValue() != null && e.getValue().equals(c))
				{
					encodedData.add(e.getKey());
					break;
				}
			}
		}
		int[] res = new int[encodedData.size()];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = encodedData.get(i).intValue();
		}
		return asSaveableData(res);
	}
	
	public String decode(String s)
	{
		int[] data = asRawData(s);
		String res = "";
		for(int i = 0; i < data.length; i++)
		{
			int encodingMapId = 0;
			while(data[i] == 65535)
			{
				encodingMapId++;
				i++;
			}
			try
			{
				res += encodings.get(encodingMapId).get(data[i]).charValue();
			}
			catch(Exception e) {}
		}
		return res;
	}
	
	private int[] asRawData(String data)
	{
		char[] c = data.toCharArray();
		int[] res = new int[c.length];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = c[i];
		}
		return res;
	}
	
	private String asSaveableData(int[] data)
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
		int i = 65535;
		while(encodings.get(mappingId).get(i--) == null)
			continue;
		// 2 is added to i to negate the decrementation during the last comparison
		// that returns true and the last comparison before the while loop ends
		return i + 2;
	}
	
	public int getFreeMappingSpaces(int mappingId)
	{
		if(mappingId >= encodings.size())
			throw new NullPointerException("Mapping with id " + mappingId + " does not exist.");
		int offset = 0;
		for(Character c : encodings.get(mappingId).values())
		{
			if(c == null)
				offset++;
		}
		return 65535 - offset;
	}
	
	/**
	 * gets the next free mapping in the designated map
	 * @param mappingId the id of the map
	 * @return the index of the next free mapping or -1 if none are found
	 */
	public int getNextFreeMappingSpace(int mappingId)
	{
		if(mappingId >= encodings.size())
			throw new NullPointerException("Mapping with id " + mappingId + " does not exist.");
		for(int i = 0; i < encodings.get(mappingId).size(); i++)
		{
			if(encodings.get(mappingId).get(i) == null)
				return i;
		}
		return -1;
	}
	
	public int getNumberOfMaps()
	{
		return encodings.size();
	}
	
	public TreeMap<Integer, Character> getMap(int mapId)
	{
		try
		{
			return encodings.get(mapId);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			throw new NullPointerException("Mapping with id " + mapId + " does not exist.");
		}
	}
	
	public static Character[] charArrayToCharacterArray(char[] ca)
	{
		Character[] res = new Character[ca.length];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = ca[i];
		}
		return res;
	}
}