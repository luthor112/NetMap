using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NETHF
{
    /// <summary>
    /// Contains references to the performance counters
    /// </summary>
    public class PerformanceCounterManager
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="PerformanceCounterManager" /> class
        /// Connects to the performance counters and reinitializes them
        /// </summary>
        /// <param name="sessionName">Name of the game session</param>
        /// <param name="categoryName">Name of the category for performance counters</param>
        public PerformanceCounterManager(string sessionName, string categoryName)
        {
            this.SessionName = sessionName;
            this.CategoryName = categoryName;

            // Connect to performance counters in write mode
            this.Objects = new PerformanceCounter(this.CategoryName, "Objects", this.SessionName, false);
            this.Threads = new PerformanceCounter(this.CategoryName, "Threads", this.SessionName, false);
            this.AvgFail = new PerformanceCounter(this.CategoryName, "AvgFail", this.SessionName, false);
            this.AvgFailBase = new PerformanceCounter(this.CategoryName, "AvgFailBase", this.SessionName, false);
            this.ProcessingRate = new PerformanceCounter(this.CategoryName, "ProcessingRate", this.SessionName, false);

            // Reinitialize performance counters
            this.Threads.RawValue = 0;
            this.ProcessingRate.RawValue = 0;
        }

        /// <summary>
        /// Gets the name of the game session supplied in the startup parameters of the application
        /// </summary>
        public string SessionName { get; private set; }

        /// <summary>
        /// Gets the name of the category the performance counters are in
        /// </summary>
        public string CategoryName { get; private set; }

        /// <summary>
        /// Gets performance counter for objects in the database
        /// </summary>
        public PerformanceCounter Objects { get; private set; }

        /// <summary>
        /// Gets performance counter for threads
        /// </summary>
        public PerformanceCounter Threads { get; private set; }

        /// <summary>
        /// Gets performance counter for fails, used for calculating average
        /// </summary>
        public PerformanceCounter AvgFail { get; private set; }

        /// <summary>
        /// Gets performance counter for processed objects, used for calculating average
        /// </summary>
        public PerformanceCounter AvgFailBase { get; private set; }

        /// <summary>
        /// Gets performance counter for processed objects, used for calculating rate
        /// </summary>
        public PerformanceCounter ProcessingRate { get; private set; }
    }
}
