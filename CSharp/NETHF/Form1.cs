using common;
using SKYPE4COMLib;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.SqlClient;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Xml;

namespace NETHF
{
    public partial class Form1 : Form
    {
        protected IDatabaseWrapper database;
        protected WorkerThreadCollection wtc;
        protected Skype skype;
        
        public static PerformanceCounterManager pcm;
        
        /// <summary>
        /// Object for locking where the access of a RawValue is necessary
        /// </summary>
        public static object lockObject = new object();

        public Form1()
        {
            InitializeComponent();
            
            database = new DatabaseWrapper("Data Source=.;Initial Catalog=master;Integrated Security=SSPI;");
            wtc = new WorkerThreadCollection();

            comboBox1.Items.Add(new CultureInfo("en"));
            comboBox1.Items.Add(new CultureInfo("hu"));

            notifyIcon1.Icon = this.Icon;
            graph1.Completed += (origin) => notifyIcon1.ShowBalloonTip(5000, "NetMap", Strings.GraphDrawCompletedTooltipText, ToolTipIcon.Info);
            graph1.Error += (origin, errorText) => notifyIcon1.ShowBalloonTip(5000, "NetMap", errorText, ToolTipIcon.Error);

            pcm = new PerformanceCounterManager(Process.GetCurrentProcess().Id.ToString(), "NETHF_NetMap");
            
            skype = new Skype();
            try
            {
                skype.Attach();
            }
            catch (COMException) { }
            ((_ISkypeEvents_Event)skype).AttachmentStatus += OnAttachmentStatus;
            ((_ISkypeEvents_Event)skype).MessageStatus += OnMessageStatus;
        }

        public void OnAttachmentStatus(TAttachmentStatus status)
        {
            if (checkBox4.Checked && status == TAttachmentStatus.apiAttachAvailable)
            {
                try
                {
                    skype.Attach();
                }
                catch (COMException ex)
                {
                    MessageBox.Show(Strings.SkypeErrorText + ex.Message, Strings.SkypeErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
                    checkBox4.Checked = false;
                }
            }
            else if (status != TAttachmentStatus.apiAttachSuccess && status != TAttachmentStatus.apiAttachPendingAuthorization)
            {
                checkBox4.Checked = false;
            }
        }

        public void OnMessageStatus(ChatMessage message, TChatMessageStatus status)
        {
            if (checkBox4.Checked &&
                (status == TChatMessageStatus.cmsSending || status == TChatMessageStatus.cmsReceived))
            {
                if (message.Body.Equals("netmap.addthread"))
                    button1.PerformClick();
                else if (message.Body.Equals("netmap.remthread"))
                    button2.PerformClick();
                else if (message.Body.Equals("netmap.draw"))
                    button3.PerformClick();
                else if (message.Body.Equals("netmap.clear"))
                    button7.PerformClick();
                else if (message.Body.Contains(" "))
                {
                    if (message.Body.Substring(0, message.Body.IndexOf(' ')).Equals("netmap.add"))
                    {
                        Task.Factory.StartNew(() => button6_Click_Run(message.Body.Substring(message.Body.IndexOf(' ') + 1)));
                    }
                }
                else if (message.Body.Equals("netmap.loveme"))
                {
                    message.Chat.SendMessage(Strings.EasterEggText);
                }
            }
        }

        protected Options createOptions()
        {
            return new Options(checkBox1.Checked, checkBox2.Checked, checkBox3.Checked, textBox1.Text);
        }

        private void button1_Click(object sender, EventArgs e)
        {
            wtc.Add(new WorkerThread(database, createOptions(), wtc));
            Form1.pcm.Threads.Increment();
        }

        private void button2_Click(object sender, EventArgs e)
        {
            if (wtc.Count > 0)
            {
                wtc[0].endWork();
                wtc.RemoveAt(0);
                Form1.pcm.Threads.Decrement();
            }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            graph1.newGraph(database, createOptions().serversOnly, Program.localization);
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            timer1.Enabled = false;
            Task.Factory.StartNew(() => timer1_Tick_Run());
        }

        protected void timer1_Tick_Run()
        {
            string newText;
            int entryCount;

            Thread.CurrentThread.CurrentCulture = Program.localization;
            Thread.CurrentThread.CurrentUICulture = Program.localization;

            try
            {
                entryCount = database.entryCount();
                
                lock (lockObject)
                    Form1.pcm.Objects.RawValue = entryCount;

                newText = entryCount.ToString() + Strings.StatusObjNumText +
                                  wtc.Count.ToString() + Strings.StatusThreadNumText;
            }
            catch (SqlException)
            {
                newText = Strings.StatusObjNumErrorText + wtc.Count.ToString() + Strings.StatusThreadNumText;
            }
            
            if (InvokeRequired)
            {
                if (!IsHandleCreated)
                {
                    CreateHandle();
                }
                Invoke(new Action(() => {
                    label1.Text = newText;
                    timer1.Enabled = true;
                }));
            }
            else
            {
                label1.Text = newText;
                timer1.Enabled = true;
            }
        }

        private void button5_Click(object sender, EventArgs e)
        {
            XmlDocument xmlDoc = new XmlDocument();
            var rootNode = xmlDoc.CreateElement("options");
            xmlDoc.AppendChild(rootNode);

            var linksNode = xmlDoc.CreateElement("linksonly");
            linksNode.SetAttribute("value", checkBox1.Checked.ToString());

            var serversNode = xmlDoc.CreateElement("serversonly");
            serversNode.SetAttribute("value", checkBox2.Checked.ToString());

            var noLeaveNode = xmlDoc.CreateElement("noleave");
            noLeaveNode.SetAttribute("value", checkBox3.Checked.ToString());
            noLeaveNode.SetAttribute("boundTo", textBox1.Text);

            var skypeNode = xmlDoc.CreateElement("skype");
            skypeNode.SetAttribute("value", checkBox4.Checked.ToString());

            rootNode.AppendChild(linksNode);
            rootNode.AppendChild(serversNode);
            rootNode.AppendChild(noLeaveNode);
            rootNode.AppendChild(skypeNode);

            xmlDoc.Save("nethf.xml");
        }

        private void button4_Click(object sender, EventArgs e)
        {
            if (File.Exists("nethf.xml"))
            {
                XmlDocument xmlDoc = new XmlDocument();
                xmlDoc.Load("nethf.xml");

                XmlNode linksNode = xmlDoc.SelectSingleNode("/options/linksonly");
                string linksNodeState = linksNode.Attributes["value"].Value;
                if (linksNodeState.Equals("True"))
                    checkBox1.Checked = true;
                else
                    checkBox1.Checked = false;

                XmlNode serversNode = xmlDoc.SelectSingleNode("/options/serversonly");
                string serversNodeState = serversNode.Attributes["value"].Value;
                if (serversNodeState.Equals("True"))
                    checkBox2.Checked = true;
                else
                    checkBox2.Checked = false;

                XmlNode boundNode = xmlDoc.SelectSingleNode("/options/noleave");
                string boundNodeState = boundNode.Attributes["value"].Value;
                if (boundNodeState.Equals("True"))
                    checkBox3.Checked = true;
                else
                    checkBox3.Checked = false;
                textBox1.Text = boundNode.Attributes["boundTo"].Value;

                XmlNode skypeNode = xmlDoc.SelectSingleNode("/options/skype");
                string skypeNodeState = skypeNode.Attributes["value"].Value;
                if (skypeNodeState.Equals("True"))
                    checkBox4.Checked = true;
                else
                    checkBox4.Checked = false;
            }
        }

        private void button7_Click(object sender, EventArgs e)
        {
            Task.Factory.StartNew(() => button7_Click_Run());
        }

        protected void button7_Click_Run()
        {
            Thread.CurrentThread.CurrentCulture = Program.localization;
            Thread.CurrentThread.CurrentUICulture = Program.localization;

            try
            {
                database.clear();
                Program.Logger.Write("Database cleared.", "Interaction", 0, 104, TraceEventType.Information);
            }
            catch (SqlException)
            {
                Program.Logger.Write("Error clearing the database.", "Interaction", 0, 105, TraceEventType.Error);
                MessageBox.Show(Strings.DbClearErrorText, Strings.DbClearErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void button6_Click(object sender, EventArgs e)
        {
            Task.Factory.StartNew(() => button6_Click_Run(textBox2.Text));
        }

        protected void button6_Click_Run(string URL)
        {
            Uri newURL = null;

            Thread.CurrentThread.CurrentCulture = Program.localization;
            Thread.CurrentThread.CurrentUICulture = Program.localization;

            try
            {
                newURL = new Uri(URL);
            }
            catch (UriFormatException)
            {
                Program.Logger.Write("Error adding new target: Malformed URL! - " + URL, "Interaction", 0, 101, TraceEventType.Error);
                MessageBox.Show(Strings.MalformedURLErrorText, Strings.MalformedURLErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

            if (newURL != null)
            {
                try
                {
                    database.putNew(new DatabaseEntry(newURL), 0, createOptions().serversOnly);
                    Program.Logger.Write("New target added: " + URL, "Interaction", 0, 103, TraceEventType.Information);
                }
                catch (SqlException)
                {
                    Program.Logger.Write("Error adding new target: Database Error! - " + URL, "Interaction", 0, 102, TraceEventType.Error);
                    MessageBox.Show(Strings.TargetAddErrorText, Strings.TargetAddErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
        }

        private void checkBox4_CheckedChanged(object sender, EventArgs e)
        {
            TAttachmentStatus status = ((ISkype)skype).AttachmentStatus;

            if (checkBox4.Checked)
                OnAttachmentStatus(status);
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            checkBox4.Checked = false;
            timer1.Enabled = false;
            wtc.shutdownAll();
            lock (lockObject)
                Form1.pcm.Threads.RawValue = 0;
        }

        private void comboBox1_SelectionChangeCommitted(object sender, EventArgs e)
        {
            XmlDocument xmlDoc = new XmlDocument();
            var rootNode = xmlDoc.CreateElement("localization");
            xmlDoc.AppendChild(rootNode);

            var langNode = xmlDoc.CreateElement("language");
            langNode.SetAttribute("value", ((CultureInfo)(comboBox1.SelectedItem)).Name);

            rootNode.AppendChild(langNode);

            xmlDoc.Save("lang.xml");

            System.Windows.Forms.Application.Restart();
        }
    }
}
