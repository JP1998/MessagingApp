namespace Messaging
{
    partial class MainFrame
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.Windows.Forms.Panel panel1;
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainFrame));
            this.toolbar_serversettings_item = new System.Windows.Forms.PictureBox();
            this.label1 = new System.Windows.Forms.Label();
            this.toolbar_adminmessage_item = new System.Windows.Forms.PictureBox();
            this.toolbar_ping_item = new System.Windows.Forms.PictureBox();
            this.toolbar_settings_item = new System.Windows.Forms.PictureBox();
            this.toolbar_deletemessages_item = new System.Windows.Forms.PictureBox();
            this.sendAdminMessageToolTip = new System.Windows.Forms.ToolTip(this.components);
            this.application_toolstrip = new System.Windows.Forms.ToolStrip();
            this.toolStripSplitButton1 = new System.Windows.Forms.ToolStripSplitButton();
            this.showNamesToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.refreshToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.connectedUsersButton = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.toolStrip_MessageLabel = new System.Windows.Forms.ToolStripLabel();
            this.toolStrip_ActionButton = new System.Windows.Forms.ToolStripButton();
            this.pingServerToolTip = new System.Windows.Forms.ToolTip(this.components);
            this.settingsToolTip = new System.Windows.Forms.ToolTip(this.components);
            this.deleteMessagesToolTip = new System.Windows.Forms.ToolTip(this.components);
            this.messagebar_messagetextbox = new System.Windows.Forms.RichTextBox();
            this.messagesTextBox = new System.Windows.Forms.RichTextBox();
            this.messagebar_sendbutton = new System.Windows.Forms.Button();
            this.serversettingsToolTip = new System.Windows.Forms.ToolTip(this.components);
            panel1 = new System.Windows.Forms.Panel();
            panel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_serversettings_item)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_adminmessage_item)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_ping_item)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_settings_item)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_deletemessages_item)).BeginInit();
            this.application_toolstrip.SuspendLayout();
            this.SuspendLayout();
            // 
            // panel1
            // 
            panel1.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            panel1.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(255)))), ((int)(((byte)(87)))), ((int)(((byte)(34)))));
            panel1.Controls.Add(this.toolbar_serversettings_item);
            panel1.Controls.Add(this.label1);
            panel1.Controls.Add(this.toolbar_adminmessage_item);
            panel1.Controls.Add(this.toolbar_ping_item);
            panel1.Controls.Add(this.toolbar_settings_item);
            panel1.Controls.Add(this.toolbar_deletemessages_item);
            panel1.Location = new System.Drawing.Point(-7, -10);
            panel1.Name = "panel1";
            panel1.Size = new System.Drawing.Size(998, 51);
            panel1.TabIndex = 0;
            // 
            // toolbar_serversettings_item
            // 
            this.toolbar_serversettings_item.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.toolbar_serversettings_item.BackgroundImage = global::Messaging.Properties.Resources.ic_serversettings;
            this.toolbar_serversettings_item.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.toolbar_serversettings_item.Location = new System.Drawing.Point(950, 13);
            this.toolbar_serversettings_item.Name = "toolbar_serversettings_item";
            this.toolbar_serversettings_item.Size = new System.Drawing.Size(31, 31);
            this.toolbar_serversettings_item.TabIndex = 7;
            this.toolbar_serversettings_item.TabStop = false;
            this.serversettingsToolTip.SetToolTip(this.toolbar_serversettings_item, "Show the settings regarding the server used for communication.");
            this.toolbar_serversettings_item.Click += new System.EventHandler(this.toolbar_serversettings_item_Click);
            // 
            // label1
            // 
            this.label1.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.ForeColor = System.Drawing.Color.White;
            this.label1.Location = new System.Drawing.Point(19, 18);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(100, 26);
            this.label1.TabIndex = 6;
            this.label1.Text = "Messaging";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // toolbar_adminmessage_item
            // 
            this.toolbar_adminmessage_item.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.toolbar_adminmessage_item.BackgroundImage = ((System.Drawing.Image)(resources.GetObject("toolbar_adminmessage_item.BackgroundImage")));
            this.toolbar_adminmessage_item.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.toolbar_adminmessage_item.Location = new System.Drawing.Point(801, 13);
            this.toolbar_adminmessage_item.Name = "toolbar_adminmessage_item";
            this.toolbar_adminmessage_item.Size = new System.Drawing.Size(31, 31);
            this.toolbar_adminmessage_item.TabIndex = 4;
            this.toolbar_adminmessage_item.TabStop = false;
            this.sendAdminMessageToolTip.SetToolTip(this.toolbar_adminmessage_item, "Send an admin message.\r\nRequires admin rights.\r\n");
            this.toolbar_adminmessage_item.Click += new System.EventHandler(this.toolbar_adminmessage_item_Click);
            // 
            // toolbar_ping_item
            // 
            this.toolbar_ping_item.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.toolbar_ping_item.BackgroundImage = global::Messaging.Properties.Resources.ic_ping;
            this.toolbar_ping_item.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.toolbar_ping_item.Location = new System.Drawing.Point(838, 13);
            this.toolbar_ping_item.Name = "toolbar_ping_item";
            this.toolbar_ping_item.Size = new System.Drawing.Size(31, 31);
            this.toolbar_ping_item.TabIndex = 3;
            this.toolbar_ping_item.TabStop = false;
            this.pingServerToolTip.SetToolTip(this.toolbar_ping_item, "Determine whether connection to the server is established.\r\n");
            this.toolbar_ping_item.Click += new System.EventHandler(this.toolbar_ping_item_Click);
            // 
            // toolbar_settings_item
            // 
            this.toolbar_settings_item.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.toolbar_settings_item.BackgroundImage = global::Messaging.Properties.Resources.ic_settings;
            this.toolbar_settings_item.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.toolbar_settings_item.Location = new System.Drawing.Point(912, 13);
            this.toolbar_settings_item.Name = "toolbar_settings_item";
            this.toolbar_settings_item.Size = new System.Drawing.Size(31, 31);
            this.toolbar_settings_item.TabIndex = 2;
            this.toolbar_settings_item.TabStop = false;
            this.settingsToolTip.SetToolTip(this.toolbar_settings_item, "Show the settings.");
            this.toolbar_settings_item.Click += new System.EventHandler(this.toolbar_settings_item_Click);
            // 
            // toolbar_deletemessages_item
            // 
            this.toolbar_deletemessages_item.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.toolbar_deletemessages_item.BackgroundImage = global::Messaging.Properties.Resources.ic_deletemessages;
            this.toolbar_deletemessages_item.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.toolbar_deletemessages_item.Location = new System.Drawing.Point(875, 13);
            this.toolbar_deletemessages_item.Name = "toolbar_deletemessages_item";
            this.toolbar_deletemessages_item.Size = new System.Drawing.Size(31, 31);
            this.toolbar_deletemessages_item.TabIndex = 1;
            this.toolbar_deletemessages_item.TabStop = false;
            this.deleteMessagesToolTip.SetToolTip(this.toolbar_deletemessages_item, "Delete all the messages currently displayed.\r\nWARNING: This action is irreversibl" +
        "e!");
            this.toolbar_deletemessages_item.Click += new System.EventHandler(this.toolbar_deletemessages_item_Click);
            // 
            // application_toolstrip
            // 
            this.application_toolstrip.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.application_toolstrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripSplitButton1,
            this.connectedUsersButton,
            this.toolStripSeparator1,
            this.toolStrip_MessageLabel,
            this.toolStrip_ActionButton});
            this.application_toolstrip.Location = new System.Drawing.Point(0, 537);
            this.application_toolstrip.Name = "application_toolstrip";
            this.application_toolstrip.Size = new System.Drawing.Size(984, 25);
            this.application_toolstrip.TabIndex = 1;
            this.application_toolstrip.Text = "toolStrip1";
            // 
            // toolStripSplitButton1
            // 
            this.toolStripSplitButton1.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.None;
            this.toolStripSplitButton1.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.showNamesToolStripMenuItem,
            this.refreshToolStripMenuItem});
            this.toolStripSplitButton1.Image = ((System.Drawing.Image)(resources.GetObject("toolStripSplitButton1.Image")));
            this.toolStripSplitButton1.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStripSplitButton1.Name = "toolStripSplitButton1";
            this.toolStripSplitButton1.Size = new System.Drawing.Size(16, 22);
            this.toolStripSplitButton1.Text = "toolStripSplitButton1";
            // 
            // showNamesToolStripMenuItem
            // 
            this.showNamesToolStripMenuItem.Name = "showNamesToolStripMenuItem";
            this.showNamesToolStripMenuItem.Size = new System.Drawing.Size(141, 22);
            this.showNamesToolStripMenuItem.Text = "Show names";
            this.showNamesToolStripMenuItem.Click += new System.EventHandler(this.showNamesToolStripMenuItem_Click);
            // 
            // refreshToolStripMenuItem
            // 
            this.refreshToolStripMenuItem.Name = "refreshToolStripMenuItem";
            this.refreshToolStripMenuItem.Size = new System.Drawing.Size(141, 22);
            this.refreshToolStripMenuItem.Text = "Refresh";
            this.refreshToolStripMenuItem.Click += new System.EventHandler(this.refreshToolStripMenuItem_Click);
            // 
            // connectedUsersButton
            // 
            this.connectedUsersButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Text;
            this.connectedUsersButton.Image = ((System.Drawing.Image)(resources.GetObject("connectedUsersButton.Image")));
            this.connectedUsersButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.connectedUsersButton.Name = "connectedUsersButton";
            this.connectedUsersButton.Size = new System.Drawing.Size(106, 22);
            this.connectedUsersButton.Text = "0 connected users";
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(6, 25);
            // 
            // toolStrip_MessageLabel
            // 
            this.toolStrip_MessageLabel.Name = "toolStrip_MessageLabel";
            this.toolStrip_MessageLabel.Size = new System.Drawing.Size(39, 22);
            this.toolStrip_MessageLabel.Text = "Ready";
            // 
            // toolStrip_ActionButton
            // 
            this.toolStrip_ActionButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Text;
            this.toolStrip_ActionButton.Image = ((System.Drawing.Image)(resources.GetObject("toolStrip_ActionButton.Image")));
            this.toolStrip_ActionButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.toolStrip_ActionButton.Name = "toolStrip_ActionButton";
            this.toolStrip_ActionButton.Size = new System.Drawing.Size(23, 22);
            this.toolStrip_ActionButton.Visible = false;
            this.toolStrip_ActionButton.Click += new System.EventHandler(this.toolStrip_ActionButton_Click);
            // 
            // messagebar_messagetextbox
            // 
            this.messagebar_messagetextbox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.messagebar_messagetextbox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.messagebar_messagetextbox.Location = new System.Drawing.Point(12, 505);
            this.messagebar_messagetextbox.Name = "messagebar_messagetextbox";
            this.messagebar_messagetextbox.Size = new System.Drawing.Size(887, 29);
            this.messagebar_messagetextbox.TabIndex = 2;
            this.messagebar_messagetextbox.Text = "";
            this.messagebar_messagetextbox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.messagebar_messagetextbox_KeyDown);
            // 
            // messagesTextBox
            // 
            this.messagesTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.messagesTextBox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.messagesTextBox.Font = new System.Drawing.Font("Consolas", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.messagesTextBox.Location = new System.Drawing.Point(12, 47);
            this.messagesTextBox.Name = "messagesTextBox";
            this.messagesTextBox.ReadOnly = true;
            this.messagesTextBox.Size = new System.Drawing.Size(961, 452);
            this.messagesTextBox.TabIndex = 4;
            this.messagesTextBox.Text = "";
            // 
            // messagebar_sendbutton
            // 
            this.messagebar_sendbutton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.messagebar_sendbutton.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(76)))), ((int)(((byte)(175)))), ((int)(((byte)(80)))));
            this.messagebar_sendbutton.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.messagebar_sendbutton.Image = global::Messaging.Properties.Resources.post_control_message;
            this.messagebar_sendbutton.Location = new System.Drawing.Point(906, 505);
            this.messagebar_sendbutton.Name = "messagebar_sendbutton";
            this.messagebar_sendbutton.Size = new System.Drawing.Size(67, 29);
            this.messagebar_sendbutton.TabIndex = 3;
            this.messagebar_sendbutton.UseVisualStyleBackColor = false;
            this.messagebar_sendbutton.Click += new System.EventHandler(this.messagebar_sendbutton_Click);
            // 
            // MainFrame
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(984, 562);
            this.Controls.Add(this.messagesTextBox);
            this.Controls.Add(this.messagebar_sendbutton);
            this.Controls.Add(this.messagebar_messagetextbox);
            this.Controls.Add(this.application_toolstrip);
            this.Controls.Add(panel1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "MainFrame";
            this.Text = "Messaging - Windows Client";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.ApplicationClosing);
            panel1.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_serversettings_item)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_adminmessage_item)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_ping_item)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_settings_item)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.toolbar_deletemessages_item)).EndInit();
            this.application_toolstrip.ResumeLayout(false);
            this.application_toolstrip.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion
        private System.Windows.Forms.PictureBox toolbar_deletemessages_item;
        private System.Windows.Forms.PictureBox toolbar_settings_item;
        private System.Windows.Forms.PictureBox toolbar_ping_item;
        private System.Windows.Forms.PictureBox toolbar_adminmessage_item;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.ToolTip sendAdminMessageToolTip;
        private System.Windows.Forms.ToolStrip application_toolstrip;
        private System.Windows.Forms.ToolStripSplitButton toolStripSplitButton1;
        private System.Windows.Forms.ToolStripButton connectedUsersButton;
        private System.Windows.Forms.ToolStripMenuItem showNamesToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem refreshToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripLabel toolStrip_MessageLabel;
        private System.Windows.Forms.ToolTip pingServerToolTip;
        private System.Windows.Forms.ToolTip settingsToolTip;
        private System.Windows.Forms.ToolTip deleteMessagesToolTip;
        private System.Windows.Forms.RichTextBox messagebar_messagetextbox;
        private System.Windows.Forms.Button messagebar_sendbutton;
        private System.Windows.Forms.RichTextBox messagesTextBox;
        private System.Windows.Forms.PictureBox toolbar_serversettings_item;
        private System.Windows.Forms.ToolTip serversettingsToolTip;
        private System.Windows.Forms.ToolStripButton toolStrip_ActionButton;
    }
}

