<!--
var _info = navigator.userAgent;
var _ns = false;
var _ns6 = false;
var _ie = (_info.indexOf("MSIE") > 0 && _info.indexOf("Win") > 0 && _info.indexOf("Windows 3.1") < 0);
if (_info.indexOf("Opera") > 0) _ie = false;
var _ns = (navigator.appName.indexOf("Netscape") >= 0 && ((_info.indexOf("Win") > 0 && _info.indexOf("Win16") < 0) || (_info.indexOf("Sun") > 0) || (_info.indexOf("Linux") > 0) || (_info.indexOf("AIX") > 0) || (_info.indexOf("OS/2") > 0) || (_info.indexOf("IRIX") > 0)));
var _ns6 = ((_ns == true) && (_info.indexOf("Mozilla/5") >= 0));
if (_ie == true) {
  document.writeln('<OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" WIDTH="275" HEIGHT="348" NAME="player" codebase="http://java.sun.com/update/1.4.2/jinstall-1_4-windows-i586.cab#Version=1,4,0,0">');
}
else if (_ns == true && _ns6 == false) { 
  // BEGIN: Update parameters below for NETSCAPE 3.x and 4.x support.
  document.write('<EMBED ');
  document.write('type="application/x-java-applet;version=1.4" ');
  document.write('CODE="javazoom.jlgui.player.amp.PlayerApplet.class" ');
  document.write('JAVA_CODEBASE="./" ');
  document.write('ARCHIVE="lib/jlguiapplet2.3.2.jar,lib/jlgui2.3.2-light.jar,lib/tritonus_share.jar,lib/basicplayer2.3.jar,lib/mp3spi1.9.2.jar,lib/jl1.0.jar,lib/vorbisspi1.0.1.jar,lib/jorbis-0.0.13.jar,lib/jogg-0.0.7.jar,lib/commons-logging-api.jar" ');
  document.write('NAME="player" ');
  document.write('WIDTH="275" ');
  document.write('HEIGHT="348" ');
  document.write('song="playlist.m3u" ');
  document.write('start="no" ');
  document.write('skin="skins/bao.wsz" ');
  document.write('init="jlgui.ini" ');
  document.write('location="url" ');
  document.write('useragent="winampMPEG/2.7" ');
  document.write('scriptable=true ');
  document.writeln('pluginspage="http://java.sun.com/products/plugin/index.html#download"><NOEMBED>');
  // END
}
else {
  document.writeln('<APPLET CODE="javazoom.jlgui.player.amp.PlayerApplet.class" JAVA_CODEBASE="./" ARCHIVE="lib/jlguiapplet2.3.2.jar,lib/jlgui2.3.2-light.jar,lib/tritonus_share.jar,lib/basicplayer2.3.jar,lib/mp3spi1.9.2.jar,lib/jl1.0.jar,lib/vorbisspi1.0.1.jar,lib/jorbis-0.0.13.jar,lib/jogg-0.0.7.jar,lib/commons-logging-api.jar" WIDTH="275" HEIGHT="348" NAME="player">');
}
// BEGIN: Update parameters below for INTERNET EXPLORER, FIREFOX, SAFARI, OPERA, MOZILLA, NETSCAPE 6+ support.
document.writeln('<PARAM NAME=CODE VALUE="javazoom.jlgui.player.amp.PlayerApplet.class">');
document.writeln('<PARAM NAME=CODEBASE VALUE="./">');
document.writeln('<PARAM NAME=ARCHIVE VALUE="lib/jlguiapplet2.3.2.jar,lib/jlgui2.3.2-light.jar,lib/tritonus_share.jar,lib/basicplayer2.3.jar,lib/mp3spi1.9.2.jar,lib/jl1.0.jar,lib/vorbisspi1.0.1.jar,lib/jorbis-0.0.13.jar,lib/jogg-0.0.7.jar,lib/commons-logging-api.jar">');
document.writeln('<PARAM NAME=NAME VALUE="player">');
document.writeln('<PARAM NAME="type" VALUE="application/x-java-applet;version=1.4">');
document.writeln('<PARAM NAME="scriptable" VALUE="true">');
document.writeln('<PARAM NAME="song" VALUE="playlist.m3u">');
document.writeln('<PARAM NAME="start" VALUE="no">');
document.writeln('<PARAM NAME="skin" VALUE="skins/bao.wsz">');
document.writeln('<PARAM NAME="init" VALUE="jlgui.ini">');
document.writeln('<PARAM NAME="location" VALUE="url">');
document.writeln('<PARAM NAME="useragent" VALUE="winampMPEG/2.7">');
// END
if (_ie == true) {
  document.writeln('</OBJECT>');
}
else if (_ns == true && _ns6 == false) {
  document.writeln('</NOEMBED></EMBED>');
}
else {
  document.writeln('</APPLET>');
}
//-->
