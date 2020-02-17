package org.nfctools.spi.acs;

import com.acs.smartcard.ReaderException;

import org.nfctools.api.ApduTag;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.DataBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagOpcode;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

import java.io.IOException;

public class AcrMfUlNTAGReaderWriter  implements MfUlReaderWriter {

    private ApduTag tag;
    private NfcNtag ntag;
    private int version;

    private int maxTransceiveLength = 253;

    public AcrMfUlNTAGReaderWriter(ApduTag tag, NfcNtag ntag, int version) {
        this.tag = tag;
        this.ntag = ntag;
        this.version = version;
    }

    public byte[] transmit(byte[] data) throws ReaderException {
        return tag.transmit(data);
    }

    public Response transmit(Command command) throws ReaderException {
        return tag.transmit(command);
    }

    @Override
    public MfBlock[] readBlock(int startPage, int pagesToRead) throws IOException, ReaderException {
        MfBlock[] returnBlocks = new MfBlock[pagesToRead];

        int pagesPerRead = getMaxPagesPerRead();
        int pageSize = 4;

        int reads = pagesToRead / pagesPerRead;
        if(pagesToRead % pagesPerRead != 0) {
            reads++;
        }
        for (int i = 0; i < reads; i++) {
            int range = Math.min(pagesPerRead, pagesToRead - (i * pagesPerRead));

            byte[] data = ntag.fastRead(startPage + i * pagesPerRead, startPage + i * pagesPerRead + range);

            for(int k = 0; k < range; k++) {
                byte[] block = new byte[pageSize];
                System.arraycopy(data, k * pageSize, block, 0, pageSize);
                returnBlocks[(i * pageSize) + k] = new DataBlock(block);
            }
        }

        return returnBlocks;
    }

    @Override
    public void writeBlock(int startPage, MfBlock... mfBlock) throws IOException {
        for (int currentBlock = 0; currentBlock < mfBlock.length; currentBlock++) {
            int blockNumber = startPage + currentBlock;

            Command writeBlock = new Command(Apdu.INS_UPDATE_BINARY, 0x00, blockNumber, mfBlock[currentBlock].getData());
            Response writeBlockResponse = tag.transmit(writeBlock);

            if (writeBlockResponse.isFailure()) {
                throw new MfException("Writing block failed. Page: " + blockNumber + ", Response: "
                        + writeBlockResponse);
            }
        }
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int getMaxPagesPerRead() {
        /*
        Remark: The FAST_READ command is able to read out the whole memory with one
        command. Nevertheless, receive buffer of the NFC device must be able to handle the
        requested amount of data as there is no chaining possibility.
        */
        return maxTransceiveLength / 4 - 4;
    }

    @Override
    public byte[] transmitPassthrough(byte[] data) {
        return tag.transmitPassthrough(data);
    }

}
