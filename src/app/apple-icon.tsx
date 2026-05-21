import { ImageResponse } from "next/og";

export const size = {
  width: 180,
  height: 180,
};

export const contentType = "image/png";

export default function AppleIcon() {
  return new ImageResponse(
    (
      <div
        style={{
          alignItems: "center",
          background: "#f3efe5",
          color: "#13261a",
          display: "flex",
          fontFamily: "Arial, sans-serif",
          fontSize: 56,
          fontWeight: 800,
          height: "100%",
          justifyContent: "center",
          width: "100%",
        }}
      >
        S
      </div>
    ),
    size,
  );
}
