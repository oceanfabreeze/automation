terraform {
  required_providers {
    esxi = {
      source  = "josenk/esxi"
      version = "1.9.0"
    }
  }
}

provider "esxi" {
  esxi_hostname = "xxxxxxxxx"
  esxi_username = "xxxxxxxxx"
  esxi_password = "xxxxxxxxx"
}

locals {
  vms = [
    {
      name    = "finder"
      cpus    = 1
      memory  = 2048
      disk    = 16
    },
    {
      name    = "station"
      cpus    = 48
      memory  = 32768
      disk    = 100
    },
    {
      name    = "uplink"
      cpus    = 6
      memory  = 16384
      disk    = 500
    }
  ]
}

resource "esxi_guest" "rhel" {
  for_each = { for vm in local.vms : vm.name => vm }

  guest_name    = each.key
  disk_store    = "TempDatastore"
  network_name  = "VM Network"
  guestos       = "rhel9_64Guest"
  memsize       = each.value.memory
  numvcpus      = each.value.cpus
  disk_size     = each.value.disk
  clone_from_vm = "rhel9-base"
  power         = "on"

  # Pass hostname to cloud-init
  extra_config = {
    "guestinfo.metadata"          = base64encode(jsonencode({ local_hostname = each.key }))
    "guestinfo.metadata.encoding" = "base64"
  }
}
