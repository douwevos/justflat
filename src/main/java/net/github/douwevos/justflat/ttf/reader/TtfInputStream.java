package net.github.douwevos.justflat.ttf.reader;

import java.io.EOFException;
import java.io.IOException;

import net.github.douwevos.justflat.ttf.format.types.F2Dot14;
import net.github.douwevos.justflat.ttf.format.types.FWord;
import net.github.douwevos.justflat.ttf.format.types.Fixed;
import net.github.douwevos.justflat.ttf.format.types.Int16;
import net.github.douwevos.justflat.ttf.format.types.LongDateTime;
import net.github.douwevos.justflat.ttf.format.types.UFWord;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.format.types.Uint32;

public class TtfInputStream {

	private byte data[];
	final byte helpBuffer[] = new byte[16];
	int seekOffset;

	public TtfInputStream(byte data[]) {
		this.data = data;
	}

	public long readLong() throws IOException {
		if (read(helpBuffer, 0, 8) != 8) {
			throw new EOFException("could not read an uint from stream");
		}

		long val = (helpBuffer[0] & 0xFF) << 24;
		val = val << 8 + (helpBuffer[1] & 0xFF);
		val = val << 8 + (helpBuffer[2] & 0xFF);
		val = val << 8 + (helpBuffer[3] & 0xFF);
		val = val << 8 + (helpBuffer[4] & 0xFF);
		val = val << 8 + (helpBuffer[5] & 0xFF);
		val = val << 8 + (helpBuffer[6] & 0xFF);
		val = val << 8 + (helpBuffer[7] & 0xFF);
		return val;
	}

	public LongDateTime readLongDateTime() throws IOException {
		return new LongDateTime(readLong());
	}

	public int readInt() throws IOException {
		if (read(helpBuffer, 0, 4) != 4) {
			throw new EOFException("could not read an uint from stream");
		}
		int val = (helpBuffer[0] & 0xFF) << 24;
		val += (helpBuffer[1] & 0xFF) << 16;
		val += (helpBuffer[2] & 0xFF) << 8;
		val += (helpBuffer[3] & 0xFF);
		return val;
	}

	public Uint32 readUint32() throws IOException {
		return new Uint32(readInt());
	}

	public short readShort() throws IOException {
		short val = (short) (read() << 8);
		val += read();
		return val;
	}

	public F2Dot14 readF2Dot14() throws IOException {
		return new F2Dot14(readShort());
	}

	public Uint16 readUint16() throws IOException {
		return new Uint16(readShort());
	}

	public FWord readFWord() throws IOException {
		return new FWord(readShort());
	}

	public UFWord readUFWord() throws IOException {
		return new UFWord(readShort());
	}

	public Int16 readInt16() throws IOException {
		return new Int16(readShort());
	}

	public Fixed readFixed() throws IOException {
		return new Fixed(readInt());
	}

	public int read() throws IOException {
		if (seekOffset>=data.length) {
			throw new EOFException();
		}
		return 0xFF & data[seekOffset++];
	}

	public int read(byte[] b) {
		int readCnt = 0;
		while(seekOffset<data.length && readCnt<b.length) {
			b[readCnt++] = data[seekOffset++];
		}
		return readCnt;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int readCnt = 0;
		while(seekOffset<data.length && readCnt<len) {
			b[off+readCnt++] = data[seekOffset++];
		}
		return readCnt;
	}

	public int available() throws IOException {
		return data.length-seekOffset;
	}

	public void setSeek(int seekOffset) {
		this.seekOffset = seekOffset;
	}

	public int getSeekOffset() {
		return seekOffset;
	}
	
	public int dataLength() {
		return data.length;
	}

}
