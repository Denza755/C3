package net.sf.l2j.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;

import net.sf.l2j.Config;

public class Status extends Thread
{
  private ServerSocket    statusServerSocket;

  private int             _uptime;
  private int             _StatusPort;
  private String          _StatusPW;    
  public static StatusThread telnetlist;

  public void run()
  {
    while (true)
    {
      try
      {
        Socket connection = statusServerSocket.accept();
                
        new StatusThread(connection, _uptime, _StatusPW);

        if (this.isInterrupted())
        {
          try
          {
            statusServerSocket.close();
          }
          catch (IOException io) { io.printStackTrace(); }
          break;
        }
      }
      catch (IOException e)
      {
        if (this.isInterrupted())
        {
          try
          {
            statusServerSocket.close();
          }
          catch (IOException io) { io.printStackTrace(); }
          break;
        }
      }
    }
  }
    
  public Status() throws IOException
  {
    super("Status");
    Properties telnetSettings = new Properties();
    InputStream is = new FileInputStream(net.sf.l2j.Server.fortressHomeDir+Config.TELNET_FILE);
    telnetSettings.load(is);
    is.close();
        
    _StatusPort       = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
    _StatusPW         = telnetSettings.getProperty("StatusPW");
        
    if (_StatusPW == null)
    {
      System.out.println("Server's Telnet Function Has No Password Defined!");
      System.out.println("A Password Has Been Automaticly Created!");
      _StatusPW = RndPW(10);
      System.out.println("Password Has Been Set To: " + _StatusPW);
    }
    statusServerSocket = new ServerSocket(_StatusPort);
    _uptime = (int) System.currentTimeMillis();
    System.out.println("StatusServer Started! - Listening on Port: " + _StatusPort);
    System.out.println("Password Has Been Set To: " + _StatusPW);
  }
  
  private String RndPW(int length)
  {
    StringBuffer password = new StringBuffer();
    String lowerChar= "qwertyuiopasdfghjklzxcvbnm";
    String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
    String digits = "1234567890";
    Random randInt = new Random();
    for (int i = 0; i < length; i++)
    {
      int charSet = randInt.nextInt(3);
      switch (charSet)
      {
        case 0:
          password.append(lowerChar.charAt(randInt.nextInt(lowerChar.length()-1)));
          break;
        case 1:
          password.append(upperChar.charAt(randInt.nextInt(upperChar.length()-1)));
          break;
        case 2:
          password.append(digits.charAt(randInt.nextInt(digits.length()-1)));
          break;
        }
      }
    return password.toString();
  }
}
