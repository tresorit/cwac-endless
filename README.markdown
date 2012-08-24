CWAC EndlessAdapter: It Just Keeps Going and Going And...
=========================================================

AJAX Web sites have sometimes taken up the "endless page"
model, where scrolling automatically loads in new content,
so you never have to click a "Next" link or anything like that.

Wouldn't it be cool to have that in an Android application?
Kinda like how the Android Market does it?

`EndlessAdapter` is one approach to solving this problem.

It is designed to wrap around another adapter, where you have
your "real" data. Hence, it follows the Decorator pattern,
augmenting your current adapter with new Endless Technology(TM).

To use it, you extend `EndlessAdapter` to provide details about
how to handle the endlessness. Specifically, you need to be
able to provide a row `View`, independent from any of the rows
in your actual adapter, that will serve as a placeholder
while you, in another method, load in the actual data to
your main adapter. Then, with a little help from you, it
seamlessly transitions in the new data.

So, this is not truly "endless" insofar as the user does see
when we load in new data. However, it should work well for
Android applications backed by Web services or the like
that work on "page-at-a-time" metaphors -- users get the
additional data quickly and do not incur the bandwidth to
download that data until and unless they scroll all the
way to the bottom.

Note that this has been tested with `ArrayAdapter` extensively
but may not work with other adapter types, particularly
`SimpleAdapter`. It also will only work with a `ListView` or
possibly other one-`View`-at-a-time `AdapterView` implementations.

This is available as a JAR file from the downloads area of this GitHub repo.
The project itself is set up as an Android library project,
in case you wish to use the source code in that fashion.

Usage
-----
To use `EndlessAdapter`, you need to create a subclass that
will control the endlessness, specifying what `View` to use
for the "loading" placeholder, and then updating that placeholder
with an actual row once data has been loaded.

`EndlessAdapter` assumes there is at least one more "batch" of
data to be fetched. If everything was retrieved for your
`ListAdapter` the first time out (e.g., the Web search returned
only one "page" of results), do not wrap it in `EndlessAdapter`,
and your users will not perceive a difference.

### Constructors

`EndlessAdapter` has two constructors. The original one takes a `ListAdapter` as
a parameter, representing the existing adapter to be made
endless. Your `EndlessAdapter` subclass will need to override
this constructor and chain upwards. For example, the DemoAdapter
inside the demo project takes an `ArrayList<String>` as a
constructor parameter and wraps it in a `ListAdapter` to supply
to `EndlessAdapter`.

The second constructor takes a `Context` and resource ID along with
the `ListAdapter`. These will be used to create the placeholder
(see below).

### The Placeholder

Your `EndlessAdapter` subclass can implement `getPendingView()`.
This method works a bit like the traditional `getView()`, in that
it receives a `ViewGroup` parameter and is supposed to return a
row `View`. The major difference is that this method needs to
return a row `View` that can serve as a placeholder, indicating
to the user that you are fetching more data in the background
(see below). This `View` is not cached by `EndlessAdapter`, so
if you wish to reuse it, cache it yourself.

If you use the constructor that takes a `Context` and resource ID along with
the `ListAdapter`, you can skip `getPendingView()`, and `EndlessAdapter`
will inflate the supplied layout resource as needed to create
this placeholder.

### The Loading

Your `EndlessAdapter` subclass also needs to implement `cacheInBackground()`.
This method will be called from a background thread, and it needs
to download more data that will eventually be added to the `ListAdapter`
you used in the constructor. While the demo application simply sleeps for 10 seconds, a real
application might make a Web service call or otherwise load in
more data.

This method returns a `boolean`, which needs to be `true` if there
is more data yet to be fetched, `false` otherwise.

Since this method is called on a background thread, you do not
need to fork your own thread. However, at the same time, do not
try to update the UI directly.

If you expected to be able to retrieve data, but failed (e.g., network
error), that is fine. However, you should then return `false`, indicating
that you have no more data.

### The Attaching

Your `EndlessAdapter` subclass also needs to implement `appendCachedData()`,
which should take the data cached by `cacheInBackground()` and append
it to the `ListAdapter` you used in the constructor. While
`cacheInBackground()` is called on a background thread,
`appendCachedData()` is called on the main application thread.

If you had a network error in `cacheInBackground()`, simply do nothing
in `appendCachedData()`. So long as you returned `false` from
`cacheInBackground()`, `EndlessAdapter` will remove the placeholder
`View` and will operate as a normal fixed-length list. Or,
override `onException()` to get control on the main application
thread and be passed the `Exception` raised by `cacheInBackground()`,
so you can do something to let the user know what went wrong.
Have `onException()` return `true` if you want to retry loading data in the background,
`false` otherwise.

*If* you returned `false` from `onException()` *and* whatever circumstances
caused the exception should now be resolved (e.g., you now have Internet
access where before you did not), call `restartAppending()`, and the
normal "endless" behavior will resume on the next scroll-to-the-bottom.

### The Threading

By default, `EndlessAdapter` will use `AsyncTask` with the classic
thread pool. If you would prefer your `EndlessAdapter` use the
serialized pool on API Level 13+ projects, call `setSerialized(true)`.

And if that paragraph was clear as mud,
[here is a blog post covering the changes to `AsyncTask`](http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html)
that pertain to the serialized pool.

If you wish to extend what is done in this `AsyncTask`, create your
own subclass of the static `EndlessAdapter.AppendTask`, implement what you need
(chaining to the superclass to inherit existing behavior), and
override `buildTask()` in your `EndlessAdapter` subclass to create
an instance of your own custom task class.

If you would prefer that `EndlessAdapter` *not* run its own `AsyncTask`,
then call `setRunInBackground(false)`. In this mode, your `cacheInBackground()`
method will be called **on the main application thread**. It is up to you
to arrange to do the work on your own background thread, then call `onDataReady()`
when you want the adapter to update to reflect the newly added data. Note
that `appendCachedData()` will not be used in this scenario.

Dependencies
------------
This project relies upon the [CWAC AdapterWrapper][adapter] project.
A copy of compatible JARs can be found in the `libs/` directory
of the project, though you are welcome to try newer ones, or
ones that you have patched yourself.

Version
-------
This is version v1.0 of this module. Booyah!

(note: not to be confused with [OUYA](http://www.ouya.tv/))

Demo
----
In the `demo/` sub-project you will find
three sample activities that demonstrate the use of `EndlessAdapter`.
Included in this is `EndlessAdapterFragmentDemo`, which shows how
to use `EndlessAdapter` in a retained fragment. Note that while
the `demo/` sample requires API Level 11 (as `EndlessAdapterFragmentDemo`
uses native fragments and the native action bar), `EndlessAdapter`
should work back to API Level 3.

Note that when you build the JAR via `ant jar`, the sample
activity is not included, nor any resources -- only the
compiled classes for the actual library are put into the JAR.

License
-------
The code in this project is licensed under the Apache
Software License 2.0, per the terms of the included LICENSE
file.

Questions
---------
If you have questions regarding the use of this code, please post a question
on [StackOverflow](http://stackoverflow.com/questions/ask) tagged with `commonsware` and `android`. Be sure to indicate
what CWAC module you are having issues with, and be sure to include source code 
and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, please post an [issue](https://github.com/commonsguy/cwac-endless/issues). Be certain to include complete steps
for reproducing the issue.

Do not ask for help via Twitter.

Release Notes
-------------
* v1.0.0: made this the official 1.0 release
* v0.10.0: added support for `setRunInBackground()`, cleaned up demos a bit
* v0.9.1: made `AppendTask` constructor `protected`
* v0.9.0: added `restartAppending()` and `buildTask()`, refactored `AppendTask`, added new sample activity
* v0.8.0: added `setSerialized()` and `isSerialized()`
* v0.7.0: `cacheInBackground()` can now throw checked exceptions, new `getContext()` method available for subclasses
* v0.6.1: merged bug fix from rgladwell/cwac-endless; added @Override annotations
* v0.6.0: added pending `View` support via constructor
* v0.5.0: added `onException()`
* v0.4.0: eliminated need for `rebindPendingView()`, documented the no-data scenario
* v0.3.1: fixed bug in manifest
* v0.3.0: converted to Android library project, added call to `notifyDataSetChanged()`

Who Made This?
--------------
<a href="http://commonsware.com">![CommonsWare](http://commonsware.com/images/logo.png)</a>

[adapter]: http://github.com/commonsguy/cwac-adapter/tree/master
