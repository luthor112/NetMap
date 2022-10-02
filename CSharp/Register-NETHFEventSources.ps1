# Name:   Register-NETHFEventSources.ps1
# Edited by: Hornyak Zsolt
# Creation Date:   2013.06.06.
# Desc:   Registers the event sources for the NetMap application

# check that we have are admins
$identity=[System.Security.Principal.WindowsIdentity]::GetCurrent()
$principal=new-object System.Security.Principal.WindowsPrincipal($identity)
$adminRole=[System.Security.Principal.WindowsBuiltInRole]::Administrator

if (! $principal.IsInRole($adminRole))
{
    echo "This script should be run as administrator!"
    return
}

[System.Diagnostics.EventLog]::CreateEventSource("NETHF_NetMap", "Application")
