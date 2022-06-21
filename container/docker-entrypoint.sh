#!/bin/sh

# If the USER env var is set, configure the user and drop privs to it
# Setting UID and/or GID env vars allow more control over created user
# Default to common first-user uid/gid of 1000
if [ -n "${USER:-}" ]; then
  addgroup "${USER}" -g "${GID:-"${UID:=1000}"}"
  adduser "${USER}" -G "${USER}" -u "${UID}" -D
  # Chown files under USER home which aren't owned by USER
  find "$(getent passwd "${USER}" | awk -F : '{ print $6 }')" ! -user "${USER}" -exec chown "${USER}:${USER}" {} \;
  # Switch to USER and run the passed-in command/args after loading /etc/profile to fixup paths, etc
  # This approach retains passed-in env vars
  su "${USER}" -c "source ${ENV:-/etc/profile} ; ${*}"
else
  exec "$@"
fi
