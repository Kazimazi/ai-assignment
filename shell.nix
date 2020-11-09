{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    jdk14
    # jdt-language-server not yet in nixpkgs
  ];
}
