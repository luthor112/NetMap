using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace common
{
    public class Point
    {
        public int x;
        public int y;

        public Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public Point()
            : this(0, 0) { }
    }

    public class DatabaseEntry
    {
        public Uri URL;
        public HashSet<Uri> links;
        public int ID;
        public Point coord;

        public DatabaseEntry(Uri URL, HashSet<Uri> links, int ID)
        {
            this.URL = URL;
            this.links = links;
            this.ID = ID;
            this.coord = null;
        }

        public DatabaseEntry(Uri URL, HashSet<Uri> links)
            : this(URL, links, 0) { }

        public DatabaseEntry(Uri URL)
            : this(URL, new HashSet<Uri>(), 0) { }

        public DatabaseEntry()
            : this(null, null, 0) { }
    }
}
