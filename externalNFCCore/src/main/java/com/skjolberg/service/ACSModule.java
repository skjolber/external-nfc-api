package com.skjolberg.service;

/*=========================================================================================
'   Company         :   Advanced Card Systems Ltd.
'
'   Author          :   Arturo B. Salvamante Jr.
'
'   Module          :   ACSModule.java
'
'   Date            :   March 19, 2008
'
'   Revision Trail  :   Date / Author / Description
'
'
'=========================================================================================*/

public class ACSModule {

    public static final int SCARD_S_SUCCESS = 0;
    public static final int SCARD_ATR_LENGTH = 33;
//        public static int SCARDCONTEXT;
//      	public static int SCARDHANDLE;

    //public class SCARDCONTEXT

    public class APDURec {
        public byte bCLA;

        /// <summary>
        /// An instruction code in the T=0 instruction class.
        /// </summary>
        public byte bINS;

        /// <summary>
        /// Reference codes that complete the instruction code.
        /// </summary>
        public byte bP1;

        /// <summary>
        /// Reference codes that complete the instruction code.
        /// </summary>
        public byte bP2;

        /// <summary>
        /// The number of data bytes to be transmitted during the command, per ISO 7816-4, Section 8.2.1.
        /// </summary>
        public byte bP3;

        public byte[] Data;

        public byte[] SW;
    }

    /// <summary>
    /// The SCARD_IO_REQUEST structure begins a protocol control information structure. Any protocol-specific information then immediately follows this structure. The entire length of the structure must be aligned with the underlying hardware architecture word size. For example, in Win32 the length of any PCI information must be a multiple of four bytes so that it aligns on a 32-bit boundary.
    /// </summary>
    static class SCARD_IO_REQUEST {
        /// <summary>
        /// Protocol in use.
        /// </summary>
        int dwProtocol;

        /// <summary>
        /// Length, in bytes, of the SCARD_IO_REQUEST structure plus any following PCI-specific information.
        /// </summary>
        int cbPciLength;
    }

    /// <summary>
    /// The SCARD_READERSTATE structure is used by functions for tracking smart cards within readers.
    /// </summary>
    static class SCARD_READERSTATE {
        /// <summary>
        /// Pointer to the name of the reader being monitored.
        /// </summary>
        String RdrName;
        /// <summary>
        /// Not used by the smart card subsystem. This member is used by the application.
        /// </summary>
        int UserData;

        /// <summary>
        /// Current state of the reader, as seen by the application. This field can take on any of the following values, in combination, as a bit mask.
        /// </summary>
        int RdrCurrState;

        /// <summary>
        /// Current state of the reader, as known by the smart card resource manager. This field can take on any of the following values, in combination, as a bit mask.
        /// </summary>
        int RdrEventState;

        /// <summary>
        /// Number of bytes in the returned ATR.
        /// </summary>
        int ATRLength;


        /// <summary>
        /// ATR of the inserted card, with extra alignment bytes.
        /// </summary>
        byte[] ATRValue;

        public SCARD_READERSTATE() {
        }
    }

    ///Memory Card Type
    public static final int CT_MCU = 0x00;                   // MCU
    public static final int CT_IIC_Auto = 0x01;               // IIC (Auto Detect Memory Size)
    public static final int CT_IIC_1K = 0x02;                 // IIC (1K)
    public static final int CT_IIC_2K = 0x03;                 // IIC (2K)
    public static final int CT_IIC_4K = 0x04;                 // IIC (4K)
    public static final int CT_IIC_8K = 0x05;                 // IIC (8K)
    public static final int CT_IIC_16K = 0x06;                // IIC (16K)
    public static final int CT_IIC_32K = 0x07;                // IIC (32K)
    public static final int CT_IIC_64K = 0x08;                // IIC (64K)
    public static final int CT_IIC_128K = 0x09;               // IIC (128K)
    public static final int CT_IIC_256K = 0x0A;               // IIC (256K)
    public static final int CT_IIC_512K = 0x0B;               // IIC (512K)
    public static final int CT_IIC_1024K = 0x0C;              // IIC (1024K)
    public static final int CT_AT88SC153 = 0x0D;              // AT88SC153
    public static final int CT_AT88SC1608 = 0x0E;             // AT88SC1608
    public static final int CT_SLE4418 = 0x0F;                // SLE4418
    public static final int CT_SLE4428 = 0x10;                // SLE4428
    public static final int CT_SLE4432 = 0x11;                // SLE4432
    public static final int CT_SLE4442 = 0x12;                // SLE4442
    public static final int CT_SLE4406 = 0x13;                // SLE4406
    public static final int CT_SLE4436 = 0x14;                // SLE4436
    public static final int CT_SLE5536 = 0x15;                // SLE5536
    public static final int CT_MCUT0 = 0x16;                  // MCU T=0
    public static final int CT_MCUT1 = 0x17;                  // MCU T=1
    public static final int CT_MCU_Auto = 0x18;               // MCU Autodetect


    //==========================CONTEXT SCOPE================================================

    /// <summary>
    /// The context is a user context, and any database operations
    /// are performed within the domain of the user.
    /// </summary>
    public static final int SCARD_SCOPE_USER = 0;

    /// <summary>
    /// The context is that of the current terminal, and any database
    /// operations are performed within the domain of that terminal.
    /// (The calling application must have appropriate access permissions
    /// for any database actions.)
    /// </summary>
    public static final int SCARD_SCOPE_TERMINAL = 1;

    /// <summary>
    /// The context is the system context, and any database operations
    /// are performed within the domain of the system.  (The calling
    /// application must have appropriate access permissions for any
    /// database actions.)
    /// </summary>
    public static final int SCARD_SCOPE_SYSTEM = 2;

    /// <summary>
    /// The application is unaware of the current state, and would like
    /// to know. The use of this value results in an immediate return
    /// from state transition monitoring services. This is represented
    /// by all bits set to zero.
    /// </summary>
    public static final int SCARD_STATE_UNAWARE = 0x00;

    /// <summary>
    /// The application requested that this reader be ignored. No other
    /// bits will be set.
    /// </summary>
    public static final int SCARD_STATE_IGNORE = 0x01;

    /// <summary>
    /// This implies that there is a difference between the state
    /// believed by the application, and the state known by the Service
    /// Manager.When this bit is set, the application may assume a
    /// significant state change has occurred on this reader.
    /// </summary>
    public static final int SCARD_STATE_CHANGED = 0x02;

    /// <summary>
    /// This implies that the given reader name is not recognized by
    /// the Service Manager. If this bit is set, then SCARD_STATE_CHANGED
    /// and SCARD_STATE_IGNORE will also be set.
    /// </summary>
    public static final int SCARD_STATE_UNKNOWN = 0x04;

    /// <summary>
    /// This implies that the actual state of this reader is not
    /// available. If this bit is set, then all the following bits are
    /// clear.
    /// </summary>
    public static final int SCARD_STATE_UNAVAILABLE = 0x08;

    /// <summary>
    /// This implies that there is not card in the reader.  If this bit
    /// is set, all the following bits will be clear.
    /// </summary>
    public static final int SCARD_STATE_EMPTY = 0x10;

    /// <summary>
    /// This implies that there is a card in the reader.
    /// </summary>
    public static final int SCARD_STATE_PRESENT = 0x20;

    /// <summary>
    /// This implies that there is a card in the reader with an ATR
    /// matching one of the target cards. If this bit is set,
    /// SCARD_STATE_PRESENT will also be set.  This bit is only returned
    /// on the SCardLocateCard() service.
    /// </summary>
    public static final int SCARD_STATE_ATRMATCH = 0x40;

    /// <summary>
    /// This implies that the card in the reader is allocated for
    /// exclusive use by another application. If this bit is set,
    /// SCARD_STATE_PRESENT will also be set.
    /// </summary>
    public static final int SCARD_STATE_EXCLUSIVE = 0x80;

    /// <summary>
    /// This implies that the card in the reader is in use by one or
    /// more other applications, but may be connected to in shared mode.
    /// If this bit is set, SCARD_STATE_PRESENT will also be set.
    /// </summary>
    public static final int SCARD_STATE_INUSE = 0x100;

    /// <summary>
    /// This implies that the card in the reader is unresponsive or not
    /// supported by the reader or software.
    /// </summary>
    public static final int SCARD_STATE_MUTE = 0x200;

    /// <summary>
    /// This implies that the card in the reader has not been powered up.
    /// </summary>
    public static final int SCARD_STATE_UNPOWERED = 0x400;

    /// <summary>
    /// This application is not willing to share this card with other
    /// applications.
    /// </summary>
    public static final int SCARD_SHARE_EXCLUSIVE = 1;

    /// <summary>
    /// This application is willing to share this card with other
    /// applications.
    /// </summary>
    public static final int SCARD_SHARE_SHARED = 2;

    /// <summary>
    /// This application demands direct control of the reader, so it
    /// is not available to other applications.
    /// </summary>
    public static final int SCARD_SHARE_DIRECT = 3;


    //================================Disposition=============================================

    /// <summary>
    /// Don't do anything special on close
    /// </summary>
    public static final int SCARD_LEAVE_CARD = 0;

    /// <summary>
    /// Reset the card on close
    /// </summary>
    public static final int SCARD_RESET_CARD = 1;

    /// <summary>
    /// Power down the card on close
    /// </summary>
    public static final int SCARD_UNPOWER_CARD = 2;

    /// <summary>
    /// Eject the card on close
    /// </summary>
    public static final int SCARD_EJECT_CARD = 3;


    //=============================ACS IOCTL Class===========================================
    public static final int FILE_DEVICE_SMARTCARD = 0x310000; // Reader action IOCTLs
    public static final int IOCTL_SMARTCARD_DIRECT = FILE_DEVICE_SMARTCARD + 2050 * 4;
    public static final int IOCTL_SMARTCARD_SELECT_SLOT = FILE_DEVICE_SMARTCARD + 2051 * 4;
    public static final int IOCTL_SMARTCARD_DRAW_LCDBMP = FILE_DEVICE_SMARTCARD + 2052 * 4;
    public static final int IOCTL_SMARTCARD_DISPLAY_LCD = FILE_DEVICE_SMARTCARD + 2053 * 4;
    public static final int IOCTL_SMARTCARD_CLR_LCD = FILE_DEVICE_SMARTCARD + 2054 * 4;
    public static final int IOCTL_SMARTCARD_READ_KEYPAD = FILE_DEVICE_SMARTCARD + 2055 * 4;
    public static final int IOCTL_SMARTCARD_READ_RTC = FILE_DEVICE_SMARTCARD + 2057 * 4;
    public static final int IOCTL_SMARTCARD_SET_RTC = FILE_DEVICE_SMARTCARD + 2058 * 4;
    public static final int IOCTL_SMARTCARD_SET_OPTION = FILE_DEVICE_SMARTCARD + 2059 * 4;
    public static final int IOCTL_SMARTCARD_SET_LED = FILE_DEVICE_SMARTCARD + 2060 * 4;
    public static final int IOCTL_SMARTCARD_LOAD_KEY = FILE_DEVICE_SMARTCARD + 2062 * 4;
    public static final int IOCTL_SMARTCARD_READ_EEPROM = FILE_DEVICE_SMARTCARD + 2065 * 4;
    public static final int IOCTL_SMARTCARD_WRITE_EEPROM = FILE_DEVICE_SMARTCARD + 2066 * 4;
    public static final int IOCTL_SMARTCARD_GET_VERSION = FILE_DEVICE_SMARTCARD + 2067 * 4;
    public static final int IOCTL_SMARTCARD_GET_READER_INFO = FILE_DEVICE_SMARTCARD + 2051 * 4;
    public static final int IOCTL_SMARTCARD_SET_CARD_TYPE = FILE_DEVICE_SMARTCARD + 2060 * 4;
    public static final int IOCTL_SMARTCARD_ACR128_ESCAPE_COMMAND = FILE_DEVICE_SMARTCARD + 2079 * 4;
    public static final int IOCTL_CCID_ESCAPE_SCARD_CTL_CODE = FILE_DEVICE_SMARTCARD + 3500 * 4;

    //===================================Error Codes=========================================
    public static final int SCARD_F_INTERNAL_ERROR = -2146435071;
    public static final int SCARD_E_CANCELLED = -2146435070;
    public static final int SCARD_E_INVALID_HANDLE = -2146435069;
    public static final int SCARD_E_INVALID_PARAMETER = -2146435068;
    public static final int SCARD_E_INVALID_TARGET = -2146435067;
    public static final int SCARD_E_NO_MEMORY = -2146435066;
    public static final int SCARD_F_WAITED_TOO_LONG = -2146435065;
    public static final int SCARD_E_INSUFFICIENT_BUFFER = -2146435064;
    public static final int SCARD_E_UNKNOWN_READER = -2146435063;
    public static final int SCARD_E_NO_READERS_AVAILABLE = -2146435026;


    public static final int SCARD_E_TIMEOUT = -2146435062;
    public static final int SCARD_E_SHARING_VIOLATION = -2146435061;
    public static final int SCARD_E_NO_SMARTCARD = -2146435060;
    public static final int SCARD_E_UNKNOWN_CARD = -2146435059;
    public static final int SCARD_E_CANT_DISPOSE = -2146435058;
    public static final int SCARD_E_PROTO_MISMATCH = -2146435057;


    public static final int SCARD_E_NOT_READY = -2146435056;
    public static final int SCARD_E_INVALID_VALUE = -2146435055;
    public static final int SCARD_E_SYSTEM_CANCELLED = -2146435054;
    public static final int SCARD_F_COMM_ERROR = -2146435053;
    public static final int SCARD_F_UNKNOWN_ERROR = -2146435052;
    public static final int SCARD_E_INVALID_ATR = -2146435051;
    public static final int SCARD_E_NOT_TRANSACTED = -2146435050;
    public static final int SCARD_E_READER_UNAVAILABLE = -2146435049;
    public static final int SCARD_P_SHUTDOWN = -2146435048;
    public static final int SCARD_E_PCI_TOO_SMALL = -2146435047;

    public static final int SCARD_E_READER_UNSUPPORTED = -2146435046;
    public static final int SCARD_E_DUPLICATE_READER = -2146435045;
    public static final int SCARD_E_CARD_UNSUPPORTED = -2146435044;
    public static final int SCARD_E_NO_SERVICE = -2146435043;
    public static final int SCARD_E_SERVICE_STOPPED = -2146435042;

    public static final int SCARD_W_UNSUPPORTED_CARD = -2146435041;
    public static final int SCARD_W_UNRESPONSIVE_CARD = -2146435040;
    public static final int SCARD_W_UNPOWERED_CARD = -2146435039;
    public static final int SCARD_W_RESET_CARD = -2146435038;

    //From SCARD_W_REMOVED_CARD to SCARD_E_DIR_NOT_FOUND
    public static final int SCARD_E_DIR_NOT_FOUND = -2146435037;

    public static final int SCARD_W_REMOVED_CARD = -2146434967;


    //==============================Protocol=============================================
    /// <summary>
    /// There is no active protocol.
    /// </summary>
    public static final int SCARD_PROTOCOL_UNDEFINED = 0x00;

    /// <summary>
    /// T=0 is the active protocol.
    /// </summary>
    public static final int SCARD_PROTOCOL_T0 = 0x01;

    /// <summary>
    /// T=1 is the active protocol.
    /// </summary>
    public static final int SCARD_PROTOCOL_T1 = 0x02;

    /// <summary>
    /// Raw is the active protocol.
    /// </summary>
    public static final int SCARD_PROTOCOL_RAW = 0x10000;


    //================================Reader State=========================================
    /// <summary>
    /// This value implies the driver is unaware of the current
    /// state of the reader.
    /// </summary>
    public static final int SCARD_UNKNOWN = 0;

    /// <summary>
    /// This value implies there is no card in the reader.
    /// </summary>
    public static final int SCARD_ABSENT = 1;

    /// <summary>
    /// This value implies there is a card is present in the reader,
    /// but that it has not been moved into position for use.
    /// </summary>
    public static final int SCARD_PRESENT = 2;

    /// <summary>
    /// This value implies there is a card in the reader in position
    /// for use.  The card is not powered.
    /// </summary>
    public static final int SCARD_SWALLOWED = 3;

    /// <summary>
    /// This value implies there is power is being provided to the card,
    /// but the Reader Driver is unaware of the mode of the card.
    /// </summary>
    public static final int SCARD_POWERED = 4;

    /// <summary>
    /// This value implies the card has been reset and is awaiting
    /// PTS negotiation.
    /// </summary>
    public static final int SCARD_NEGOTIABLE = 5;

    /// <summary>
    /// This value implies the card has been reset and specific
    /// communication protocols have been established.
    /// </summary>
    public static final int SCARD_SPECIFIC = 6;


    //===========================================Miscellaneous========================================
    ///<summary>
    ///This function returns the specific error message that occurs.
    ///
    ///</summary>
    public static String GetScardErrMsg(int errCode) {
        switch (errCode) {
            case SCARD_E_CANCELLED:
                return ("The action was canceled by an SCardCancel request.");
            case SCARD_E_CANT_DISPOSE:
                return ("The system could not dispose of the media in the requested manner.");
            case SCARD_E_CARD_UNSUPPORTED:
                return ("The smart card does not meet minimal requirements for support.");
            case SCARD_E_DUPLICATE_READER:
                return ("The reader driver didn't produce a unique reader name.");
            case SCARD_E_INSUFFICIENT_BUFFER:
                return ("The data buffer for returned data is too small for the returned data.");
            case SCARD_E_INVALID_ATR:
                return ("An ATR string obtained from the registry is not a valid ATR string.");
            case SCARD_E_INVALID_HANDLE:
                return ("The supplied handle was invalid.");
            case SCARD_E_INVALID_PARAMETER:
                return ("One or more of the supplied parameters could not be properly interpreted.");
            case SCARD_E_INVALID_TARGET:
                return ("Registry startup information is missing or invalid.");
            case SCARD_E_INVALID_VALUE:
                return ("One or more of the supplied parameter values could not be properly interpreted.");
            case SCARD_E_NOT_READY:
                return ("The reader or card is not ready to accept commands.");
            case SCARD_E_NOT_TRANSACTED:
                return ("An attempt was made to end a non-existent transaction.");
            case SCARD_E_NO_MEMORY:
                return ("Not enough memory available to complete this command.");
            case SCARD_E_NO_SERVICE:
                return ("The smart card resource manager is not running.");
            case SCARD_E_NO_SMARTCARD:
                return ("The operation requires a smart card, but no smart card is currently in the device.");
            case SCARD_E_PCI_TOO_SMALL:
                return ("The PCI receive buffer was too small.");
            case SCARD_E_PROTO_MISMATCH:
                return ("The requested protocols are incompatible with the protocol currently in use with the card.");
            case SCARD_E_READER_UNAVAILABLE:
                return ("The specified reader is not currently available for use.");
            case SCARD_E_READER_UNSUPPORTED:
                return ("The reader driver does not meet minimal requirements for support.");
            case SCARD_E_SERVICE_STOPPED:
                return ("The smart card resource manager has shut down.");
            case SCARD_E_SHARING_VIOLATION:
                return ("The smart card cannot be accessed because of other outstanding connections.");
            case SCARD_E_SYSTEM_CANCELLED:
                return ("The action was canceled by the system, presumably to log off or shut down.");
            case SCARD_E_TIMEOUT:
                return ("The user-specified timeout value has expired.");
            case SCARD_E_UNKNOWN_CARD:
                return ("The specified smart card name is not recognized.");
            case SCARD_E_UNKNOWN_READER:
                return ("The specified reader name is not recognized.");
            case SCARD_E_NO_READERS_AVAILABLE:
                return ("No smart card reader is available.");
            case SCARD_F_COMM_ERROR:
                return ("An internal communications error has been detected.");
            case SCARD_F_INTERNAL_ERROR:
                return ("An internal consistency check failed.");
            case SCARD_F_UNKNOWN_ERROR:
                return ("An internal error has been detected, but the source is unknown.");
            case SCARD_F_WAITED_TOO_LONG:
                return ("An internal consistency timer has expired.");
            case SCARD_S_SUCCESS:
                return ("No error was encountered.");
            case SCARD_E_DIR_NOT_FOUND:
                return ("The identified directory does not exist in the smart card..");
            case SCARD_W_RESET_CARD:
                return ("The smart card has been reset, so any shared state information is invalid.");
            case SCARD_W_UNPOWERED_CARD:
                return ("Power has been removed from the smart card, so that further communication is not possible.");
            case SCARD_W_UNRESPONSIVE_CARD:
                return ("The smart card is not responding to a reset.");
            case SCARD_W_UNSUPPORTED_CARD:
                return ("The reader cannot communicate with the card, due to ATR string configuration conflicts.");
            case SCARD_W_REMOVED_CARD:
                return ("The smart card has been removed, so further communication is not possible.");
            default:
                return ("Code: " + errCode + "\r\nDescription: " + "Undocumented error.");
        }
    }


}
