using Microsoft.Practices.EnterpriseLibrary.Common.Configuration;
using Microsoft.Practices.EnterpriseLibrary.Logging;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Xml;

namespace NETHF
{
    static class Program
    {
        public static CultureInfo localization;
        public static LogWriter Logger;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            if (File.Exists("lang.xml"))
            {
                XmlDocument xmlDoc = new XmlDocument();
                xmlDoc.Load("lang.xml");

                XmlNode langNode = xmlDoc.SelectSingleNode("/localization/language");
                string langNodeState = langNode.Attributes["value"].Value;

                Program.localization = new CultureInfo(langNodeState);
            }
            else
            {
                Program.localization = new CultureInfo("en");
            }

            Thread.CurrentThread.CurrentCulture = Program.localization;
            Thread.CurrentThread.CurrentUICulture = Program.localization;

            Program.Logger = EnterpriseLibraryContainer.Current.GetInstance<LogWriter>();

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Form1());
        }
    }
}
