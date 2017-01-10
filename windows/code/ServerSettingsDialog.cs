using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Messaging
{
    public partial class ServerSettingsDialog : Form
    {
        public ServerSettingsDialog(string adress, int port)
        {
            InitializeComponent();

            serverAdressTextBox.Text = adress;
            serverPortTextBox.Text = "" + port;
        }

        public string getAdress()
        {
            return serverAdressTextBox.Text;
        }

        public string getPort()
        {
            return serverPortTextBox.Text;
        }

        private void saveBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }

        private void abortBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
        }
    }
}
