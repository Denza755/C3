package net.sf.l2j.loginserver.serverpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;

/**
 * Format: d
 * d: response
 */
public class GGAuth extends ServerBasePacket

{
	static Logger _log = Logger.getLogger(GGAuth.class.getName());
	public static int SKIP_GG_AUTH_REQUEST = 0x0b;

	public GGAuth(int response) 
	{
		writeC(0x0b);
		writeD(response); 
		if (Config.DEBUG) 
			_log.warning("Reason " + "Hex: "+(Integer.toHexString(response)));
	}

public byte[] getContent() {return getBytes();}

}
