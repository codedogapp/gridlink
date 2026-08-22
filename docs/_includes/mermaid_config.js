{
  "startOnLoad": false,
  "theme": (function () {
    try {
      var scheme = localStorage.getItem("jtd-color-scheme");
      if (!scheme && window.matchMedia) {
        scheme = matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
      }
      return scheme === "dark" ? "dark" : "default";
    } catch (e) {
      return "default";
    }
  })()
}
