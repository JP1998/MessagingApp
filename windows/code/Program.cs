using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Text;
using System.IO;


namespace Messaging
{
    static class Program
    {
        /// <summary>
        /// Der Haupteinstiegspunkt für die Anwendung.
        /// </summary>
        [STAThread]
        static void Main()
        {
//           /*
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainFrame());
//           */

            // Console.WriteLine(DateTime.Now.ToString());
            // Console.WriteLine(Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase));

//            TestNetworkingCode();
        }

        static void TestNetworkingCode()
        {
            string toSend1 = "Hey, das ist eine kleine Nachricht, die im Java Client gelesen werden sollte. Wie sieht däs öigentlich müt ßonderzeichen aus?";
            string toSend2 = "Das ist eine weitere Nachricht, um zu schauen, ob das auch geht :D";
            byte toLead = 6;

            TcpClient socket = new TcpClient("192.168.178.30", 45321);

            socket.GetStream().WriteByte(toLead);
            writeUTF(socket.GetStream(), toSend1);
            writeUTF(socket.GetStream(), toSend2);

            string readString = readUTF(socket.GetStream());
            Console.WriteLine(readString);
            readString = readUTF(socket.GetStream());
            Console.WriteLine(readString);

            int readInt = ReadInt(socket.GetStream());
            Console.WriteLine(readInt);
            readInt = ReadInt(socket.GetStream());
            Console.WriteLine(readInt);

            socket.Close();
        }
        
        static int ReadInt(NetworkStream connectedStream)
        {
            return ((byte)connectedStream.ReadByte()) << 24 |
                ((byte)connectedStream.ReadByte()) << 16 |
                ((byte)connectedStream.ReadByte()) << 8 |
                ((byte)connectedStream.ReadByte());
        }



        static string readUTF(NetworkStream connectedStream)
        {
            int length = connectedStream.ReadByte() * 256 + connectedStream.ReadByte();
            byte[] bytecontents = new byte[length];
            connectedStream.Read(bytecontents, 0, length);
            return Encoding.UTF8.GetString(bytecontents);
        }

        static void writeUTF(NetworkStream connectedStream, string send)
        {
            byte[] convertedStringToSend = Encoding.UTF8.GetBytes(send);

            byte[] sending = new byte[convertedStringToSend.Length + 2];

            sending[0] = (byte)(convertedStringToSend.Length / 256);
            sending[1] = (byte)(convertedStringToSend.Length % 256);

            for (int i = 2; i < sending.Length; i++)
            {
                sending[i] = convertedStringToSend[i - 2];
            }

            connectedStream.Write(sending, 0, sending.Length);
        }


    }
}
