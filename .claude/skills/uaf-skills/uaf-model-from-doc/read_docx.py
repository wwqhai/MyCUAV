"""把 docx 转成带样式标记的纯文本，便于阅读与抽取。

用法（Windows 必须设 PYTHONUTF8=1，否则中文输出会触发 gbk 编码错误）：
    PYTHONUTF8=1 python read_docx.py 输入.docx 输出.txt

输出每行格式： 样式 | 文本     （样式如 Heading1/Heading2/ListParagraph，"-" 表示正文或表格单元格）
表格单元格按出现顺序逐行输出，需要按列数自行还原成表。
"""
import re
import sys
import zipfile


def main(src, dst):
    z = zipfile.ZipFile(src)
    xml = z.read("word/document.xml").decode("utf8")
    out = []
    for p in re.findall(r"<w:p[ >].*?</w:p>", xml, flags=re.S):
        style = re.search(r'<w:pStyle w:val="([^"]+)"', p)
        text = "".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", p))
        if text.strip():
            out.append((style.group(1) if style else "-") + " | " + text)
    with open(dst, "w", encoding="utf8") as f:
        f.write("\n".join(out))
    print("段落数", len(out))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
