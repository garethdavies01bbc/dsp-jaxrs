Upgrade work to enable DPUB-993 to work with Spring 4.3.20.RELEASE

* These notes are in progress.

The basic idea for this work is to remove the old dependency cxf-bundle-jaxrs and migrate to CXF 3.* +

So far these classes have been migrated along with the accompanying tests:

HttpRequestMonitor  - now uses ContainerRequestContext and ContainerResponseContext
HttpResponseMonitor - now uses ContainerRequestFilter

There is a new dependency cxf-rt-frontend-jaxrs 3.2.0 which handles the Monitor classes mentions above.

The plan I am following is basically:

- Commenting out the cxf 2 bundle  dependency in pom.xml and then seeing what breaks
in compile and test.
- Looking up the migration notes and replacing the existing functionality with the recommended
migration to cxf 3.
- Then running tests again...seeing if I can remove cxf-bundle 2 again...etc...until finally we can remove cxf-bundle 2 without
any problems.

I think the next parts to be replaced will be the classes involving WADL. If I remove cxf-bundle these start flagging up
when running tests. The cxf 3 dependency I added may not cover all the functionality here (it may do - I have no idea!)
so you may have to hunt about for another cxf maven dependency. Lots of useful stuff on Stack Overflow and below.

Useful:

Migration bits -
https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS#JAXRS-CXF3.2.0
https://users.cxf.apache.narkive.com/2vJIPYCa/requesthandler-in-cxf-3-0

For mocking out the Intercepter I added and used Powermock.
https://www.baeldung.com/intro-to-powermock

** I found some really useful code examples in Github BBC repos and non-BBC. **