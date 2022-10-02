# Name:   Create-NETHFPerformanceCounters.ps1
# Original: Micskei Zoltan
# Edited by: Hornyak Zsolt
# Original Creation Date:   2011.04.18.
# Desc:   Creates the performance counters for the NetMap application
# Param:  -Force    deletes the counters before creating them

param(
    [switch] $Force
)

$CATEGORY_NAME = "NETHF_NetMap"

# check that we have are admins
# NOTE: we actually do not need admin rights, just the appropriate level of PerformanceCounterPermissionAccess
$identity=[System.Security.Principal.WindowsIdentity]::GetCurrent()
$principal=new-object System.Security.Principal.WindowsPrincipal($identity)
$adminRole=[System.Security.Principal.WindowsBuiltInRole]::Administrator

if (! $principal.IsInRole($adminRole))
{
    echo "This script should be run as administrator!"
    return
}

# check whether the category exists
$exists = [System.Diagnostics.PerformanceCounterCategory]::Exists($CATEGORY_NAME)
if ($exists -and ! $Force)
{
    echo "Category $CATEGORY_NAME exists, call the script with the -Force parameter to delete it before creating!"
    return
}

# delete the category if force was specified
if ($Force -and $exists)
{   
    echo "Deleting $CATEGORY_NAME category"          
    [System.Diagnostics.PerformanceCounterCategory]::Delete($CATEGORY_NAME);
}

#create counters
echo "Creating $CATEGORY_NAME category with counter"

$CounterDatas = New-Object System.Diagnostics.CounterCreationDataCollection;

$cdCounter1 = new-object System.Diagnostics.CounterCreationData;
$cdCounter1.CounterName = "Objects";
$cdCounter1.CounterHelp = "Number of objects in the database."
$cdCounter1.CounterType = [System.Diagnostics.PerformanceCounterType]::NumberOfItems64

$cdCounter2 = new-object System.Diagnostics.CounterCreationData
$cdCounter2.CounterName = "Threads"
$cdCounter2.CounterHelp = "Number of threads."
$cdCounter2.CounterType = [System.Diagnostics.PerformanceCounterType]::NumberOfItems64

$cdCounter3 = new-object System.Diagnostics.CounterCreationData
$cdCounter3.CounterName = "AvgFail"
$cdCounter3.CounterHelp = "Average number of fails."
$cdCounter3.CounterType = [System.Diagnostics.PerformanceCounterType]::AverageCount64

$cdCounter4 = new-object System.Diagnostics.CounterCreationData
$cdCounter4.CounterName = "AvgFailBase"
$cdCounter4.CounterHelp = "Number of trials."
$cdCounter4.CounterType = [System.Diagnostics.PerformanceCounterType]::AverageBase

$cdCounter5 = new-object System.Diagnostics.CounterCreationData
$cdCounter5.CounterName = "ProcessingRate"
$cdCounter5.CounterHelp = "Number of processed objects per second."
$cdCounter5.CounterType = [System.Diagnostics.PerformanceCounterType]::RateOfCountsPerSecond64

$CounterDatas.Add($cdCounter1) > $null
$CounterDatas.Add($cdCounter2) > $null
$CounterDatas.Add($cdCounter3) > $null
$CounterDatas.Add($cdCounter4) > $null
$CounterDatas.Add($cdCounter5) > $null

[System.Diagnostics.PerformanceCounterCategory]::Create($CATEGORY_NAME, "Performance counters for the NetMap application", [System.Diagnostics.PerformanceCounterCategoryType]::MultiInstance, $CounterDatas)

