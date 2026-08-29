# Third-Party and Platform Notices

The mundanereq application source has no third-party Java library dependency.
The packaged executables are nevertheless GraalVM Native Image outputs: they
include reachable standard-library classes, language runtime code, and
statically linked native code from the selected JDK.

For that reason every generated package includes, conservatively:

- `LICENSES/GraalVM-Native-Image.txt`, copied verbatim from the selected
  GraalVM distribution's `LICENSE_NATIVEIMAGE.txt`; and
- `LICENSES/GraalVM-JDK/`, a verbatim copy of that distribution's complete
  `legal/` notice tree, including module licenses, exceptions, and bundled
  third-party notices.

The project code and documentation are covered by
`LICENSES/mundanereq-BSD-3-Clause.txt`.

The executables dynamically depend on the GNU/Linux platform's glibc, dynamic
loader, and zlib. These system libraries are not copied into the package; their
licenses and updates are supplied by the target operating system.

`BUILD-ENVIRONMENT.txt` identifies the Native Image distribution and platform
used for a particular package. Changing the GraalVM distribution requires
regenerating the package from that distribution so its corresponding notices
are included.
