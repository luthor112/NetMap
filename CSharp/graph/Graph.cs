using common;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.SqlClient;
using System.Drawing;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace graph
{
    public partial class Graph : Control
    {
        protected Image currentGraph;

        public delegate void CompletedHandler(Graph origin);
        public delegate void ErrorHandler(Graph origin, string errorText);

        public event CompletedHandler Completed;
        public event ErrorHandler Error;

        public Graph()
        {
            InitializeComponent();
            currentGraph = new Bitmap(1, 1);
        }

        protected override void OnPaint(PaintEventArgs pe)
        {
            base.OnPaint(pe);
            pe.Graphics.DrawImage(currentGraph, new PointF(0, 0));
        }

        public static int pow2(int num)
        {
            int acc = 1;
            for (int i = 0; i < num; i++)
                acc *= 2;
            return acc;
        }

        public Task newGraph(IDatabaseWrapper database, bool serversOnly, CultureInfo local)
        {
            return Task.Factory.StartNew(() => genGraph(database, serversOnly, local));
        }

        protected void genGraph(IDatabaseWrapper database, bool serversOnly, CultureInfo local)
        {
            Thread.CurrentThread.CurrentCulture = local;
            Thread.CurrentThread.CurrentUICulture = local;

            try
            {
                List<DatabaseEntry> list = database.getDatabase();

                DatabaseEntry otherEnd = null;
                common.Point otherEndCoord = null;

                int circle = 0;
                int verticle = 0;
                int x = 0;
                int y = 0;

                foreach (DatabaseEntry de in list)
                {
                    x = (int)((circle * 30) * Math.Sin(((2 * Math.PI) / pow2(circle + 1)) * verticle));
                    y = (int)((circle * 30) * Math.Cos(((2 * Math.PI) / pow2(circle + 1)) * verticle));

                    de.coord = new common.Point(x, y);

                    if ((verticle + 1 == pow2(circle + 1)) || (circle == 0))
                    {
                        verticle = 0;
                        circle++;
                    }
                    else
                    {
                        verticle++;
                    }
                }

                Image newGraph = new Bitmap(circle * 60 + 11, circle * 60 + 11);
                using (Graphics graph = Graphics.FromImage(newGraph))
                {
                    graph.TranslateTransform(circle * 30 + 5, circle * 30 + 5);

                    foreach (DatabaseEntry de in list)
                    {
                        foreach (Uri url in de.links)
                        {
                            otherEnd = database.get(url, serversOnly);
                            var q = from entry in list
                                    where entry.ID == otherEnd.ID
                                    select entry.coord;
                            otherEndCoord = q.Single();

                            graph.DrawLine(Pens.Black, de.coord.x, de.coord.y, otherEndCoord.x, otherEndCoord.y);
                        }
                    }

                    foreach (DatabaseEntry de in list)
                    {
                        graph.FillEllipse(Brushes.Blue, de.coord.x - 5, de.coord.y - 5, 10, 10);
                    }
                }

                currentGraph = newGraph;
                this.Invalidate();

                if (Completed != null)
                    Completed(this);
            }
            catch (SqlException)
            {
                if (Error != null)
                    Error(this, Strings.DbFetchErrorText);
                MessageBox.Show(Strings.DbFetchErrorText, Strings.DbFetchErrorCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
