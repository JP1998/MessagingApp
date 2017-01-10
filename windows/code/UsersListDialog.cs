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
    public partial class UsersListDialog : Form
    {
        public UsersListDialog(string[] names)
        {
            InitializeComponent();

            foreach(string name in names)
            {
                usersListView.Items.Add(name);
            }
        }

        private void okButton_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }
    }
}
