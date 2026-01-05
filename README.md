# LegendasBUDA

Visualizador de legendas para vídeos do YouTube, desenvolvido para auxiliar alunos a acompanhar legendas em português enquanto assistem vídeos.

## Funcionalidades

- **Upload de legendas**: Carregue um arquivo DOCX com legendas separadas por linhas em branco
- **Navegação fácil**:
  - ESPAÇO ou → para próxima legenda
  - BACKSPACE ou ← para legenda anterior
- **Sempre visível**: Opção de manter a janela sempre no topo
- **Auto-atualização**: Sistema automático de atualizações via GitHub

## Como usar

1. Execute o aplicativo LegendasBUDA
2. Clique em "Carregar Legendas (.docx)"
3. Selecione seu arquivo DOCX com as legendas
4. Use as setas ou atalhos de teclado para navegar

## Formato do arquivo de legendas

O arquivo DOCX deve conter legendas separadas por **uma linha em branco**. Exemplo:

```
Esta é a primeira legenda.
Ela pode ter múltiplas linhas.

Esta é a segunda legenda.

Esta é a terceira legenda.
```

## Build

### Windows

```bash
build-windows-exe.bat
```

Requer:
- JDK 17+
- Maven
- WiX Toolset (para instaladores .exe)

### macOS

```bash
./build-macos-pkg.sh
```

Requer:
- JDK 17+
- Maven

## Repositório

https://github.com/tashirabten/LegendasBUDA

## Licença

Desenvolvido pela Associação BUDA
