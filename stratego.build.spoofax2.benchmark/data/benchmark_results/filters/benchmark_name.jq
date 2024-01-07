# this filter splits the problem parameter into its name and size
.[].benchmark |= split("\\."; null).[-1]