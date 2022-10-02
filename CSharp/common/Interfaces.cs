using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace common
{
    public interface IDatabaseWrapper
    {
        DatabaseEntry get(Uri URL, bool serversOnly);

        HashSet<Uri> getLinks(int URLID);

        DatabaseEntry getNonProcessedEntry();

        List<DatabaseEntry> getDatabase();

        bool putNew(DatabaseEntry entry, int state, bool serversOnly);

        bool putBack(DatabaseEntry entry, int state, bool serversOnly);

        int entryCount();

        void clear();
    }
}
