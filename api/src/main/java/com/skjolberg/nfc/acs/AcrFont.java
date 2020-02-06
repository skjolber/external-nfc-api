package com.skjolberg.nfc.acs;

public enum AcrFont {

	FontA('a', 2, 16, CharacterSets.characterSet0To127, CharacterSets.font1CharacterSet128To255),
	FontB('b', 2, 16, CharacterSets.characterSet0To127, CharacterSets.font2CharacterSet128To255), 
	FontC('c', 4, 16, CharacterSets.characterSet0To127, CharacterSets.font3CharacterSet128To255);
	
	private final char id;
	private final int lines;
	private final int lineLength;
	private final char[] mappings;

	private AcrFont(char id, int lines, int lineLength, char[] ... mappings) {
		this.id = id;
		this.lines = lines;
		this.lineLength = lineLength;
		this.mappings = CharacterSets.append(mappings);
	}

	public int getLines() {
		return lines;
	}
	
	public int getLineLength() {
		return lineLength;
	}
	
	public int getMaxCharacters() {
		return lines * lineLength;
	}
	
	public static AcrFont parse(char id) {
		for(AcrFont font : values()) {
			if(font.getId() == id) {
				return font;
			}
		}
		
		throw new IllegalArgumentException("Unknown font '" + id + "'");
	}
	
	public char getId() {
		return id;
	}
	
	public byte[] mapString(String string) {
		byte[] text = new byte[string.length()];
		
		string:
		for(int i = 0; i < text.length; i++) {
			char a = string.charAt(i);
			
			for(int k = 0; k < mappings.length; k++) {
				
				if(mappings[k] == a) {
					text[i] = (byte) (k & 0xFF);
					
					continue string;
				} else {
				
				}
			}
			
			throw new IllegalArgumentException("Unsupported character '" + a + "' at " + i + " among " + new String(mappings));
		}
		
		return text;
	}
	
	private static class CharacterSets {
		
		/** Common character for fonts 1-3 */
		private static final char[] characterSet0To127 = new char[]{
			' ', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // 0
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // 1
			' ', '!', '"','#', '$', '%', '&','\'','(', ')', '*','+',',', '-', '.','/', // 2
			'0', '1', '2', '3', '4', '5', '6','7','8', '9', ':',';','<', '=', '>','?', // 3
			'@', 'A', 'B','C', 'D', 'E', 'F','G','H', 'I', 'J','K','L', 'M', 'N','O', // 4
			'P', 'Q', 'R','S', 'T', 'U', 'V','W','X', 'Y', 'Z','[','\\', ']', '^','_', // 5
			'\'', 'a', 'b','c', 'd', 'e', 'f','g','h', 'i', 'j','k','l', 'm', 'n','o', // 6
			'p', 'q', 'r','s', 't', 'u', 'v','w','x', 'y', 'z','{','|', '}', '~',' ' // 7
		};
		
		private static final char[] font1CharacterSet128To255 = new char[]{
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // 8
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // 9
			'\n', '\n', '\n','£', '€', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // A
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', 'æ', '\n','\n', // B
			'\n', '\n', '\n','\n', 'Ä', 'Å', 'Æ','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // C
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','Ø', '\n', '\n','\n','\n', '\n', '\n','\n', // D
			'\n', '\n', '\n','\n', 'ä', 'å', 'æ','\n','\n', '\n', '\n','\n','\n', '\n', '\n','ï', // E
			'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','ø', '\n', '\n','\n','ü', '\n', '\n','ÿ' // F
		};
		
		private static final char[] font2CharacterSet128To255 = new char[]{
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','Å', // 8
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // 9
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // A
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // B
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // C
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // D
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // E
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n' // F
		};
		
		private static final char[] font3CharacterSet128To255 = new char[]{
				'\n', 'ü', '\n','å', 'ä', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', 'Ä','Å', // 8
				'\n', 'æ', 'Æ','\n', 'ö', '\n', '\n','\n','ÿ', '\n', 'ü','€','£', '\n', '\n','\n', // 9
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','Ë', '\n', '\n','\n', // A
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // B
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // C
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // D
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n', // E
				'\n', '\n', '\n','\n', '\n', '\n', '\n','\n','\n', '\n', '\n','\n','\n', '\n', '\n','\n' // F
		};
		
		private static char[] append(char[] ... mappings) {
			char[] array = new char[256];
			
			int index = 0;
			for(char[] mapping : mappings) {
				if(mapping == null) {
					throw new IllegalArgumentException();
				}
				System.arraycopy(mapping, 0, array, index, mapping.length);
				
				index += mapping.length;
			}		
			
			return array;
		}
	}
}