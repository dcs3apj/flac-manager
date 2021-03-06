# Flac Manager 

## Overview

This project can be used to manage a repository of [FLAC](http://flac.sourceforge.net/) files. 
It can do the following:

* ensure FLAC files are named consistently,
* encode FLAC files into M4A and MP3 files, making sure album artwork is copied over and
* synchronise external M4A and MP3 devices for one or more users.

## Basic Usage

The FLAC manager requires that all FLAC files are stored under one directory that is made read-only. 
FLAC files can be _checked out_ into a staging directory where they can be changed and 
then _checked in_. New FLAC files can be added to the repository by ripping them into the 
staging directory. Each FLAC file is encoded into an M4A file and an MP3 file when it is checked in to an encoded
directory and is also symbolically linked from the devices directory for each user who owns the
file.

## Server Setup

The server is distributed as a a [Docker](https://www.docker.com/) image at 
https://hub.docker.com/r/unclealex72/flac-manager. An [SQLite](http://www.sqlite.org/) database is used
to store which user owns which albums.

Your music files need to be attached as a volume at `/music` and must be writeable by a user with UID 1000. Your
music directory must have the following subdirectories:

| Subdirectory      | Contents| 
| ----------------- | ---------------------------- |
| `flac/`                       | A repository of FLAC files.  |
| `staging/`                    | A directory where new FLAC files are stored before checking in.|
| `encoded/<extension>`         | Files that are encoded versions of FLAC files. |
| `devices/<user>/<extension>/` | Symbolic links to the encoded files owned by a user. |
| `db/`                         | A directory that contains the SQLite database. |

Additionally, if you have a `tmp/` subdirectory this will be used as a temporary directory where newly encoded files
will be held. If the `tmp/` subdirectory does not exist then the system temporary directory will be used. This allows
you to decide whether it's quicker to encode to a faster file system and then non-atomically move the encoded file to 
the repository or to encode to the same file system and then atomically move the encoded file.


## Client Setup

The client can be installed as a Debian `dpkg` file.

## Usage

After installation, the following commands are available:

+ `flacman-checkout [directories]` Checkout all the FLAC files into the staging area so they can be retagged.
+ `flacman-checkin [directories]` Checkin all the FLAC files into the music library and also convert them to M4A and MP3.
+ `flacman-own --users users [directories]` Add all the FLAC files in the supplied directories (either staged or not) 
                                            to users' collections. 
  `users` is a comma separated list of user names.
+ `flacman-unown --users users [directories]` Remove all the FLAC files in the supplied directories (either staged or not) 
                                            from users' collections. 
  `users` is a comma separated list of user names.
+ `flacman-multidisc --split [directories]` Split multi-disc albums into two separate albums. The first disc will remain
    unchanged but all the others will joined in an album suffixed with _(Extras)_
+ `flacman-multidisc --join [directories]` Join multi-disc albums into a single disc.
    
## Synchronising to external devices

The Device Synchroniser project at https://github.com/unclealex72/device-synchroniser contains both Android and
desktop applications that can be used to synchronise Android phones and USB sticks respectively.

## MusicBrainz

The FLAC manager requires that all files are tagged with MusicBrainz tags 
(e.g. by [Picard](http://musicbrainz.org/doc/MusicBrainz_Picard)). This allows FLAC files to then be named consistently 
and allows more than one user to keep their external music devices synchronised.
