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
    public partial class AdminMessageDialog : Form
    {

        private const string PASSWORD = "overFL0W8:";

        public AdminMessageDialog()
        {
            InitializeComponent();
        }

        private void sendBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }

        private void abortBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Abort;
        }

        public bool hasCorrectPasswordPut()
        {
            return adminpasswordTextBox.Text.Equals(PASSWORD);
        }

        public string getMessage()
        {
            return adminmessageTextBox.Text;
        }
    }
}
