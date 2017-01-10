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
    public partial class SettingsDialog : Form
    {
        public SettingsDialog(string username, bool limit, int limitAmount)
        {
            InitializeComponent();

            userNameTextBox.Text = username;
            limitSavedMessagesCheckbox.Checked = limit;
            messagesLimitTrackbar.Value = limitAmount;
        }

        public string getUserName()
        {
            return userNameTextBox.Text;
        }

        public bool isAmountLimited()
        {
            return limitSavedMessagesCheckbox.Checked;
        }

        public int getLimit()
        {
            return messagesLimitTrackbar.Value;
        }

        private void saveBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }

        private void abortBtn_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Abort;
        }
    }
}
