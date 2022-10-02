using common;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace NETHF
{
    public class WorkerThread
    {
        protected volatile bool shutdownCommanded;
        protected volatile bool working;

        public static string quot = "\"";

        protected IDatabaseWrapper database;
        protected Options options;
        protected Thread actualThread;
        protected WorkerThreadCollection wtc;

        public WorkerThread(IDatabaseWrapper database, Options options, WorkerThreadCollection wtc)
        {
            this.database = database;
            this.options = options;
            this.wtc = wtc;

            shutdownCommanded = false;
            working = false;

            actualThread = new Thread(run);
            actualThread.Start();
        }

        public void endWork()
        {
            shutdownCommanded = true;
        }

        public bool isWorking()
        {
            return working;
        }

        protected void sendURLToDatabase(Uri newURL)
        {
            if ((!options.dontLeaveServer || (options.dontLeaveServer && newURL.Authority.Equals(options.boundTo))) && System.Text.RegularExpressions.Regex.IsMatch(newURL.AbsolutePath, ".*(htm|html|xhtm|xhtml|shtm|shtml|php|asp|aspx|cgi|jsp)", System.Text.RegularExpressions.RegexOptions.IgnoreCase))
            {
                database.putNew(new DatabaseEntry(newURL), 0, options.serversOnly);
            }
            else
            {
                database.putNew(new DatabaseEntry(newURL), 2, options.serversOnly);
            }
        }

        protected void run()
        {
            DatabaseEntry workOn = null;
            string page = null;
            string backupPage = null;
            Regex re = null;
            string addr = null;
            Uri newUri = null;

            Thread.CurrentThread.CurrentCulture = Program.localization;
            Thread.CurrentThread.CurrentUICulture = Program.localization;

            try
            {
                while (!shutdownCommanded)
                {
                    if ((workOn = database.getNonProcessedEntry()) != null)
                    {
                        working = true;

                        try
                        {
                            using (WebClient wc = new WebClient())
                            {
                                page = wc.DownloadString(workOn.URL);
                            }
                            backupPage = string.Copy(page);

                            re = new Regex(".*<a .*href=[" + quot + "'].*[" + quot + "'].*", RegexOptions.IgnoreCase);

                            while (!page.Equals(""))
                            {
                                if (re.IsMatch(page))
                                {
                                    addr = page.Substring(page.IndexOf("href=") + 6, page.IndexOf(page[page.IndexOf("href=") + 5], page.IndexOf("href=") + 6) - (page.IndexOf("href=") + 6) /*+ 1*/);
                                    page = page.Substring(page.IndexOf(page[page.IndexOf("href=") + 5], page.IndexOf("href=") + 6) + 1);
                                    newUri = new Uri(workOn.URL, addr);
                                    sendURLToDatabase(newUri);
                                    workOn.links.Add(newUri);
                                }
                                else
                                {
                                    page = "";
                                }
                            }

                            if (!options.linksOnly)
                            {
                                while (!backupPage.Equals(""))
                                {
                                    newUri = null;

                                    re = new Regex(".*" + quot + ".*://.*" + quot + ".*", RegexOptions.IgnoreCase);
                                    if (re.IsMatch(backupPage))
                                    {
                                        addr = backupPage.Substring(backupPage.LastIndexOf(quot, backupPage.IndexOf("://")) + 1, backupPage.IndexOf(quot, backupPage.IndexOf("://")) - (backupPage.LastIndexOf(quot, backupPage.IndexOf("://")) + 1) /*+ 1*/);
                                        backupPage = backupPage.Substring(backupPage.IndexOf(quot, backupPage.IndexOf("://")) + 1);
                                        newUri = new Uri(workOn.URL, addr);
                                    }
                                    else
                                    {
                                        backupPage = "";
                                    }

                                    if (newUri != null)
                                    {
                                        sendURLToDatabase(newUri);
                                        workOn.links.Add(newUri);
                                    }
                                }
                            }

                            database.putBack(workOn, 2, options.serversOnly);
                        }
                        catch (Exception)
                        {
                            database.putBack(workOn, 3, options.serversOnly);
                        }
                    }
                    else
                    {
                        working = false;
                        Thread.Sleep(5000);
                    }
                }
            }
            catch (SqlException)
            {
                MessageBox.Show(Strings.WTDbManipErrorText, Strings.WTDbManipErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
                endWork();
                if (wtc != null)
                    wtc.Remove(this);
            }

            working = false;
        }
    }

    public class WorkerThreadCollection : List<WorkerThread>
    {
        public void shutdownAll()
        {
            foreach (WorkerThread item in this)
            {
                item.endWork();
            }
        }

        public int countWorking()
        {
            int working = 0;
            foreach (WorkerThread item in this)
            {
                if (item.isWorking())
                    working++;
            }
            return working;
        }
    }

    public class Options
    {
        public bool linksOnly;
        public bool serversOnly;
        public bool dontLeaveServer;
        public string boundTo;

        public Options(bool linksOnly, bool serversOnly, bool dontLeaveServer, string boundTo)
        {
            this.linksOnly = linksOnly;
            this.serversOnly = serversOnly;
            this.dontLeaveServer = dontLeaveServer;
            this.boundTo = boundTo;
        }
    }
}
