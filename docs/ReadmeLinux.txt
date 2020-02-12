Setting up Amps and Linux virtual box
    Install Virtualbox
    Download fedora server iso
    Set up fedora instance - use fixed size disk - I gave 12GB
    Set up Fedora as host only adapter
        In VirtualBox click "File" -> "Host Network Manager" -> "Create" -> "Properties"
        Under "Adapter" then IPv4 Address enter 192.168.56.100
        IPV4 Network Mask 255.255.255.0
        Click in DHCP Server
        Tick "Enable Server"
        Under Server Address  = 192.168.56.100
        Server mask = 255.255.255.0
        Lower address = 192.168.56.101
        Upper address = 192.168.56.101
        Note: Make sure the "Server Address is different from "Lower and Upper" address
    Setup Network on the vm box
        Select VM Box
        SElect "Settings" then "Network"
        For first Adapter select "Host Only"
        For Second Adapter Select "NAT"
    Ssh from mac and run these
        ssh root@192.168.56.101
       // Remove firewall
         systemctl disable firewalld
         dnf remove firewalld
         sudo systemctl stop iptables
         sudo systemctl mask iptables
         sudo systemctl status iptables
         //update everything
         yum update
         yum install dkms
         yum install gcc
         yum distro-sync
         yum -y install kernel-devel kernel-headers dkms gcc gcc-c++
         yum install wget
         yum install psmisc
         dnf -y install git


// This did not work
    Set up Shared folder from Mac to linux
        1) First Share a "Download" folder form the mac
            In VirtualBox, click your OS on the left and click on Settings.
            Click on the Shared Folders tab.
            Click on the folder with the plus on the right.
            Browse to a folder of your choice in the folder path.
            Enter a folder name with no spaces e.g. “Share”.
            Check Auto-mount and Make Permanent, if available.
            Click on OK.
       2) Now Mount the folder in the fedora
            Create a folder in your guest OS that you want to share.
            Open up Terminal.
            Type in id and press ENTER— remember that ID.
            Switch to the root user using sudo su and enter your password.
            Browse to the etc folder using cd /etc.
            Edit the rc.local file using vi rc.local.
            Move your cursor right above exit 0 and press the letter “i” on your keyboard to insert text.
            Type in the following: sudo mount -t vboxsf -o uid=1000,gid=1000 Share /home/username/Documents/Share
                1000 should match the ID you noted down earlier.
                Share should match the folder name from step 1.
                username should match your Linux username.
                /Documents/Share should be the absolute path of the new folder you created.
            Now hit “ESC”, type :wq and hit ENTER to save and quit the file editing.

Important settings
    ROOT password  = d....k
    admin port for linux http://server:9090
    home dir /root


install box goes additions  (http://www.binarytides.com/vbox-guest-additions-fedora-20/)