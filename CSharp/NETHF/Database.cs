using common;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NETHF
{
    public class DatabaseWrapper : IDatabaseWrapper
    {
        private string connStr;

        public DatabaseWrapper(string connStr)
        {
            this.connStr = connStr;
        }

        public DatabaseEntry get(Uri URL, bool serversOnly)
        {
            lock (this)
            {
                if (serversOnly)
                {
                    using (SqlConnection conn = new SqlConnection(connStr))
                    using (SqlCommand cmd = new SqlCommand("SELECT * FROM Websites", conn))
                    {
                        cmd.CommandType = CommandType.Text;
                        conn.Open();
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                Uri tempUri = new Uri((string)reader["url"]);
                                if (tempUri.Authority.Equals(URL.Authority))
                                    return new DatabaseEntry(tempUri, getLinks((int)reader["id"]), (int)reader["id"]);
                            }
                        }
                    }
                }
                else
                {
                    using (SqlConnection conn = new SqlConnection(connStr))
                    using (SqlCommand cmd = new SqlCommand("SELECT * FROM Websites w " +
                                                           "WHERE w.url = @URL", conn))
                    {
                        cmd.CommandType = CommandType.Text;
                        cmd.Parameters.AddWithValue("@URL", URL.ToString());
                        conn.Open();
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                                return new DatabaseEntry(new Uri((string)reader["url"]), getLinks((int)reader["id"]), (int)reader["id"]);
                        }
                    }
                }
                return null;
            }
        }

        public HashSet<Uri> getLinks(int URLID)
        {
            lock (this)
            {
                HashSet<Uri> ret = new HashSet<Uri>();

                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("SELECT w.url as url " +
                                                       "FROM Websites w, WebsiteLinks l " +
                                                       "WHERE l.oneend = @URLID " +
                                                       "AND w.id = l.otherend", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    cmd.Parameters.AddWithValue("@URLID", URLID);
                    conn.Open();
                    using (SqlDataReader reader = cmd.ExecuteReader())
                    {
                        while (reader.Read())
                            ret.Add(new Uri((string)reader["url"]));
                    }
                }

                return ret;
            }
        }

        public DatabaseEntry getNonProcessedEntry()
        {
            lock (this)
            {
                DatabaseEntry ret = null;

                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("SELECT * FROM Websites w " +
                                                       "WHERE w.state = 0", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    conn.Open();
                    using (SqlDataReader reader = cmd.ExecuteReader())
                    {
                        if (reader.Read())
                            ret = new DatabaseEntry(new Uri((string)reader["url"]), getLinks((int)reader["id"]), (int)reader["id"]);
                    }
                }

                if (ret != null)
                {
                    using (SqlConnection conn = new SqlConnection(connStr))
                    using (SqlCommand cmd = new SqlCommand("UPDATE Websites " +
                                                           "SET state = 1 " +
                                                           "WHERE id = @ID", conn))
                    {
                        cmd.CommandType = CommandType.Text;
                        cmd.Parameters.AddWithValue("@ID", ret.ID);
                        conn.Open();
                        cmd.ExecuteNonQuery();
                    }

                    return ret;
                }

                return null;
            }
        }

        public List<DatabaseEntry> getDatabase()
        {
            lock (this)
            {
                List<DatabaseEntry> ret = new List<DatabaseEntry>();

                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("SELECT * FROM Websites", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    conn.Open();
                    using (SqlDataReader reader = cmd.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            ret.Add(new DatabaseEntry(new Uri((string)reader["url"]), getLinks((int)reader["id"]), (int)reader["id"]));
                        }
                    }
                }

                return ret;
            }
        }

        public bool putNew(DatabaseEntry entry, int state, bool serversOnly)
        {
            lock (this)
            {
                if (this.get(entry.URL, serversOnly) != null)
                {
                    Program.Logger.Write("Error adding new item to database: item already exists! - " + entry.URL.ToString(), "Database", 0, 201, TraceEventType.Error);
                    return false;
                }

                int rows = 0;
                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("INSERT INTO Websites (url, state) " +
                                                       "VALUES (@newURL, @newState)", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    cmd.Parameters.AddWithValue("@newURL", entry.URL.ToString());
                    cmd.Parameters.AddWithValue("@newState", state);
                    conn.Open();
                    rows = cmd.ExecuteNonQuery();
                }

                if (rows > 0)
                {
                    Program.Logger.Write("New item added to database: " + entry.URL.ToString(), "Database", 0, 202, TraceEventType.Information);
                    return true;
                }
                else
                {
                    Program.Logger.Write("Error adding new item to database: database error! - " + entry.URL.ToString(), "Database", 0, 203, TraceEventType.Error);
                    return false;
                }
            }
        }

        public bool putBack(DatabaseEntry entry, int state, bool serversOnly)
        {
            lock (this)
            {
                if (this.get(entry.URL, serversOnly) == null)
                {
                    Program.Logger.Write("Trying to update nonexistent database item! - " + entry.URL.ToString(), "Database", 0, 204, TraceEventType.Error);
                    return false;
                }

                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("UPDATE Websites " +
                                                       "SET state = @newState " +
                                                       "WHERE id = @ID", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    cmd.Parameters.AddWithValue("@ID", entry.ID);
                    cmd.Parameters.AddWithValue("@newState", state);
                    conn.Open();
                    cmd.ExecuteNonQuery();
                }

                foreach (Uri link in entry.links)
                {
                    using (SqlConnection conn = new SqlConnection(connStr))
                    using (SqlCommand cmd = new SqlCommand("INSERT INTO WebsiteLinks (oneend, otherend) " +
                                                           "VALUES (@one, @other)", conn))
                    {
                        cmd.CommandType = CommandType.Text;
                        cmd.Parameters.AddWithValue("@one", entry.ID);
                        cmd.Parameters.AddWithValue("@other", this.get(link, serversOnly).ID);
                        conn.Open();
                        cmd.ExecuteNonQuery();
                    }
                }

                if (state == 3)
                    Form1.pcm.AvgFail.Increment();
                Form1.pcm.AvgFailBase.Increment();
                Form1.pcm.ProcessingRate.Increment();

                Program.Logger.Write("Database item updated: " + entry.URL.ToString(), "Database", 0, 205, TraceEventType.Information);
                return true;
            }
        }

        public int entryCount()
        {
            lock(this)
            {
                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("SELECT COUNT(id) " +
                                                       "FROM Websites", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    conn.Open();
                    return (int)cmd.ExecuteScalar();
                }
            }
        }

        public void clear()
        {
            lock (this)
            {
                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("DELETE FROM Websites", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    conn.Open();
                    cmd.ExecuteNonQuery();
                }

                using (SqlConnection conn = new SqlConnection(connStr))
                using (SqlCommand cmd = new SqlCommand("DELETE FROM WebsiteLinks", conn))
                {
                    cmd.CommandType = CommandType.Text;
                    conn.Open();
                    cmd.ExecuteNonQuery();
                }
            }
        }
    }
}
