namespace Messaging
{
    partial class SettingsDialog
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.userNameTextBox = new System.Windows.Forms.TextBox();
            this.limitSavedMessagesCheckbox = new System.Windows.Forms.CheckBox();
            this.label2 = new System.Windows.Forms.Label();
            this.messagesLimitTrackbar = new System.Windows.Forms.TrackBar();
            this.saveBtn = new System.Windows.Forms.Button();
            this.abortBtn = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.messagesLimitTrackbar)).BeginInit();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(61, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "User name:";
            // 
            // userNameTextBox
            // 
            this.userNameTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.userNameTextBox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.userNameTextBox.Location = new System.Drawing.Point(13, 30);
            this.userNameTextBox.Name = "userNameTextBox";
            this.userNameTextBox.Size = new System.Drawing.Size(543, 20);
            this.userNameTextBox.TabIndex = 1;
            // 
            // limitSavedMessagesCheckbox
            // 
            this.limitSavedMessagesCheckbox.AutoSize = true;
            this.limitSavedMessagesCheckbox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.limitSavedMessagesCheckbox.Location = new System.Drawing.Point(16, 57);
            this.limitSavedMessagesCheckbox.Name = "limitSavedMessagesCheckbox";
            this.limitSavedMessagesCheckbox.Size = new System.Drawing.Size(182, 17);
            this.limitSavedMessagesCheckbox.TabIndex = 2;
            this.limitSavedMessagesCheckbox.Text = "Limit amount of saved messages?";
            this.limitSavedMessagesCheckbox.UseVisualStyleBackColor = true;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(16, 81);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(140, 13);
            this.label2.TabIndex = 3;
            this.label2.Text = "Amount of saved messages:";
            // 
            // messagesLimitTrackbar
            // 
            this.messagesLimitTrackbar.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.messagesLimitTrackbar.LargeChange = 100;
            this.messagesLimitTrackbar.Location = new System.Drawing.Point(13, 98);
            this.messagesLimitTrackbar.Maximum = 10000;
            this.messagesLimitTrackbar.Name = "messagesLimitTrackbar";
            this.messagesLimitTrackbar.Size = new System.Drawing.Size(543, 45);
            this.messagesLimitTrackbar.SmallChange = 10;
            this.messagesLimitTrackbar.TabIndex = 4;
            // 
            // saveBtn
            // 
            this.saveBtn.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.saveBtn.Location = new System.Drawing.Point(481, 159);
            this.saveBtn.Name = "saveBtn";
            this.saveBtn.Size = new System.Drawing.Size(75, 23);
            this.saveBtn.TabIndex = 5;
            this.saveBtn.Text = "Save";
            this.saveBtn.UseVisualStyleBackColor = true;
            this.saveBtn.Click += new System.EventHandler(this.saveBtn_Click);
            // 
            // abortBtn
            // 
            this.abortBtn.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.abortBtn.Location = new System.Drawing.Point(400, 159);
            this.abortBtn.Name = "abortBtn";
            this.abortBtn.Size = new System.Drawing.Size(75, 23);
            this.abortBtn.TabIndex = 6;
            this.abortBtn.Text = "Abort";
            this.abortBtn.UseVisualStyleBackColor = true;
            this.abortBtn.Click += new System.EventHandler(this.abortBtn_Click);
            // 
            // SettingsDialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(568, 194);
            this.Controls.Add(this.abortBtn);
            this.Controls.Add(this.saveBtn);
            this.Controls.Add(this.messagesLimitTrackbar);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.limitSavedMessagesCheckbox);
            this.Controls.Add(this.userNameTextBox);
            this.Controls.Add(this.label1);
            this.Name = "SettingsDialog";
            this.ShowIcon = false;
            this.ShowInTaskbar = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "SettingsDialog";
            ((System.ComponentModel.ISupportInitialize)(this.messagesLimitTrackbar)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox userNameTextBox;
        private System.Windows.Forms.CheckBox limitSavedMessagesCheckbox;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TrackBar messagesLimitTrackbar;
        private System.Windows.Forms.Button saveBtn;
        private System.Windows.Forms.Button abortBtn;
    }
}